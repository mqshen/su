package com.twitter.finagle.netty.proxy

import java.net.{InetSocketAddress, SocketAddress}

import com.twitter.finagle.netty.channel.ConnectPromiseDelayListeners
import io.netty.channel.{Channel, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.handler.proxy.ProxyHandler
import io.netty.util.concurrent.{Future => NettyFuture, GenericFutureListener}

/**
  * Created by goldratio on 20/05/2017.
  */
private[netty] class NettyProxyConnectHandler(
                                                 proxyHandler: ProxyHandler,
                                                 bypassLocalhostConnections: Boolean = false)
  extends ChannelOutboundHandlerAdapter with ConnectPromiseDelayListeners { self =>

  private[this] final val proxyCodecKey: String = "netty4ProxyCodec"

  private[this] final def shouldBypassProxy(isa: InetSocketAddress): Boolean =
    bypassLocalhostConnections && !isa.isUnresolved &&
      (isa.getAddress.isLoopbackAddress || isa.getAddress.isLinkLocalAddress)

  private[this] final def connectThroughProxy(
                                               ctx: ChannelHandlerContext,
                                               remote: SocketAddress,
                                               local: SocketAddress,
                                               promise: ChannelPromise
                                             ): Unit = {
    // Upgrade the pipeline with the proxy codec pieces.
    ctx.pipeline().addBefore(ctx.name(), proxyCodecKey, proxyHandler)

    val proxyConnectPromise = ctx.newPromise()

    // Cancel new promise if an original one is canceled.
    // NOTE: We don't worry about cancelling/failing pending writes here since it will happen
    // automatically on channel closure.
    promise.addListener(proxyCancellationsTo(proxyConnectPromise, ctx))

    // Fail the original promise if a new one is failed.
    // NOTE: If the connect request fails the channel was never active. Since no
    // writes are expected from the previous handler, no need to fail the pending writes.
    proxyConnectPromise.addListener(proxyFailuresTo(promise))

    // React on satisfied proxy handshake promise.
    proxyHandler.connectFuture.addListener(new GenericFutureListener[NettyFuture[Channel]] {
      override def operationComplete(future: NettyFuture[Channel]): Unit = {
        if (future.isSuccess) {
          // We "try" because it might be already cancelled and we don't need to handle
          // cancellations here - it's already done by `proxyCancellationsTo`.
          // Same thing about `tryFailure` below.
          if (promise.trySuccess()) {
            ctx.pipeline().remove(proxyCodecKey)
            ctx.pipeline().remove(self)
          }
        } else {
          // SOCKS/HTTP proxy handshake promise is failed so given `ProxyHandler` is going to
          // close the channel and fail pending writes, we only need to fail the connect promise.
          promise.tryFailure(future.cause())
        }
      }
    })

    ctx.connect(remote, local, proxyConnectPromise)
  }

  override def connect(
                        ctx: ChannelHandlerContext,
                        remote: SocketAddress,
                        local: SocketAddress,
                        promise: ChannelPromise
                      ): Unit = remote match {
    case isa: InetSocketAddress if shouldBypassProxy(isa) =>
      // We're bypassing proxies for any localhost connections.
      ctx.pipeline().remove(self)
      ctx.connect(remote, local, promise)

    case isa: InetSocketAddress if !isa.isUnresolved =>
      // We're replacing resolved InetSocketAddress with unresolved one such that
      // Netty's `HttpProxyHandler` will prefer hostname over the IP address as a destination
      // for a proxy server. This is a safer way to do HTTP proxy handshakes since not
      // all HTTP proxy servers allow for IP addresses to be passed as destinations/host headers.
      val unresolvedRemote = InetSocketAddress.createUnresolved(isa.getHostName, isa.getPort)
      connectThroughProxy(ctx, unresolvedRemote, local, promise)

    case _ =>
      connectThroughProxy(ctx, remote, local, promise)
  }

  // We don't override either `exceptionCaught` or `channelInactive` here since `ProxyHandler`
  // guarantees to fail the connect promise (which we're already handling here) if any exception
  // (caused by either inbound or outbound event or closed channel) occurs during the proxy
  // handshake.
}
