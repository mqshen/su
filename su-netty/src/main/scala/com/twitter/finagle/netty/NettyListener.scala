package com.twitter.finagle.netty

import java.lang.{Boolean => JBool, Integer => JInt}
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

import com.twitter.concurrent.NamedPoolThreadFactory
import com.twitter.finagle.netty.channel.{NettyFramedServerChannelInitializer, NettyRawServerChannelInitializer, RecvByteBufAllocatorProxy, ServerBridge}
import com.twitter.finagle.netty.transport.ChannelTransport
import com.twitter.finagle.{ListeningServer, Stack}
import com.twitter.finagle.server.Listener
import com.twitter.finagle.transport.Transport
import com.twitter.util._
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.epoll.{EpollEventLoopGroup, EpollServerSocketChannel}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel._
import io.netty.util.concurrent.{FutureListener, Future => NettyFuture}

/**
 * Created by goldratio on 19/05/2017.
 */
private[finagle] object NettyListener {
  val TrafficClass: ChannelOption[JInt] = ChannelOption.newInstance("trafficClass")

  /**
   * A [[com.twitter.finagle.Stack.Param]] used to configure the ability to
   * exert back pressure by only reading from the Channel when the [[Transport]] is
   * read.
   */
  private[finagle] case class BackPressure(enabled: Boolean) {
    def mk(): (BackPressure, Stack.Param[BackPressure]) = (this, BackPressure.param)
  }

  private[finagle] object BackPressure {
    implicit val param: Stack.Param[BackPressure] =
      Stack.Param(BackPressure(enabled = true))
  }
}

case class NettyListener[In, Out](
    pipelineInit: ChannelPipeline => Unit,
    params: Stack.Params,
    transportFactory: Channel => Transport[Any, Any] = { ch: Channel => new ChannelTransport(ch) },
    setupMarshalling: ChannelInitializer[Channel] => ChannelHandler = identity)(implicit mIn: Manifest[In], mOut: Manifest[Out]) extends Listener[In, Out] {
  import NettyListener.BackPressure

  private[this] val timer: Timer = null

  // transport params
  private[this] val Transport.Liveness(_, _, keepAlive) = params[Transport.Liveness]
  private[this] val Transport.BufferSizes(sendBufSize, recvBufSize) = params[Transport.BufferSizes]
  private[this] val Transport.Options(noDelay, reuseAddr) = params[Transport.Options]

  // listener params
  private[this] val Listener.Backlog(backlog) = params[Listener.Backlog]
  private[this] val BackPressure(backPressureEnabled) = params[BackPressure]

  private[this] val param.Allocator(allocator) = params[param.Allocator]

  override def listen(addr: SocketAddress)(serveTransport: (Transport[In, Out]) => Unit): ListeningServer = {
    new ListeningServer with CloseAwaitably {
      private[this] val bridge = new ServerBridge(
        transportFactory.andThen(Transport.cast[In, Out]),
        serveTransport
      )

      private[this] val bossLoop: EventLoopGroup =
        if (nativeEpoll.enabled)
          new EpollEventLoopGroup(
            1 /*nThreads*/ ,
            new NamedPoolThreadFactory("finagle/netty4/boss", makeDaemons = true))
        else
          new NioEventLoopGroup(
            1 /*nThreads*/ ,
            new NamedPoolThreadFactory("finagle/netty4/boss", makeDaemons = true))

      private[this] val bootstrap = new ServerBootstrap()
      if (nativeEpoll.enabled)
        bootstrap.channel(classOf[EpollServerSocketChannel])
      else
        bootstrap.channel(classOf[NioServerSocketChannel])

      bootstrap.group(bossLoop, params[param.WorkerPool].eventLoopGroup)
      bootstrap.childOption[JBool](ChannelOption.TCP_NODELAY, noDelay)

      bootstrap.option(ChannelOption.ALLOCATOR, allocator)
      bootstrap.childOption(ChannelOption.ALLOCATOR, allocator)

      // Use pooling if enabled.
      if (poolReceiveBuffers()) {
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR,
          new RecvByteBufAllocatorProxy(PooledByteBufAllocator.DEFAULT))
        bootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,
          new RecvByteBufAllocatorProxy(PooledByteBufAllocator.DEFAULT))
      }

      bootstrap.option[JBool](ChannelOption.SO_REUSEADDR, reuseAddr)
      backlog.foreach(bootstrap.option[JInt](ChannelOption.SO_BACKLOG, _))
      sendBufSize.foreach(bootstrap.childOption[JInt](ChannelOption.SO_SNDBUF, _))
      recvBufSize.foreach(bootstrap.childOption[JInt](ChannelOption.SO_RCVBUF, _))
      keepAlive.foreach(bootstrap.childOption[JBool](ChannelOption.SO_KEEPALIVE, _))
      bootstrap.childOption[JBool](ChannelOption.AUTO_READ, !backPressureEnabled)
      params[Listener.TrafficClass].value.foreach { tc =>
        bootstrap.option[JInt](NettyListener.TrafficClass, tc)
        bootstrap.childOption[JInt](NettyListener.TrafficClass, tc)
      }

      private[this] val rawInitializer = new NettyRawServerChannelInitializer(params)
      private[this] val framedInitializer = new NettyFramedServerChannelInitializer(params)


      bootstrap.childHandler(new ChannelInitializer[Channel] {
        def initChannel(ch: Channel): Unit = {

          // pipelineInit comes first so that implementors can put whatever they
          // want in pipelineInit, without having to worry about clobbering any
          // of the other handlers.
          pipelineInit(ch.pipeline)
          ch.pipeline.addLast(rawInitializer)

          // we use `setupMarshalling` to support protocols where the
          // connection is multiplexed over child channels in the
          // netty layer
          ch.pipeline.addLast("marshalling", setupMarshalling(new ChannelInitializer[Channel] {
            def initChannel(ch: Channel): Unit = {
              ch.pipeline.addLast("framedInitializer", framedInitializer)

              // The bridge handler must be last in the pipeline to ensure
              // that the bridging code sees all encoding and transformations
              // of inbound messages.
              ch.pipeline.addLast("finagleBridge", bridge)
            }
          }))
        }
      })

      private[this] val bound = bootstrap.bind(addr).awaitUninterruptibly()

      if (!bound.isSuccess)
        throw new java.net.BindException(
          s"Failed to bind to ${addr.toString}: ${bound.cause().getMessage}")

      private[this] val ch = bound.channel()

      /**
        * The address to which this server is bound.
        */
      override def boundAddress: SocketAddress = ch.localAddress()

      def closeServer(deadline: Time): Future[Unit] = closeAwaitably {
        ch.close().awaitUninterruptibly()
        val p = new Promise[Unit]
        val timeout = deadline - Time.now
        val timeoutMs = timeout.inMillis

        // The boss loop immediately starts refusing new work.
        // Existing tasks have ``timeoutMs`` time to finish executing.
        bossLoop
          .shutdownGracefully(0 /* quietPeriod */ , timeoutMs.max(0), TimeUnit.MILLISECONDS)
          .addListener(new FutureListener[Any] {
            def operationComplete(future: NettyFuture[Any]): Unit = p.setDone()
          })
        //
        p.raiseWithin(timeout)(timer).transform { _ => Future.Done }
      }
    }

  }

}
