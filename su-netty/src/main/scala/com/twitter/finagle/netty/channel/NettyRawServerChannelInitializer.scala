package com.twitter.finagle.netty.channel

import com.twitter.finagle.Stack
import com.twitter.finagle.netty.poolReceiveBuffers
import com.twitter.finagle.netty.ssl.server.NettyServerSslHandler
import com.twitter.finagle.param.{ Label, Logger, Stats }
import io.netty.channel.{ Channel, ChannelInitializer }

/**
 * Created by goldratio on 19/05/2017.
 */
private[netty] object Netty4RawServerChannelInitializer {
  val ChannelLoggerHandlerKey = "channelLogger"
  val ChannelStatsHandlerKey = "channelStats"
}

/**
 * Server channel initialization logic for the part of the netty pipeline that
 * deals with raw bytes.
 *
 * @param params [[com.twitter.finagle.Stack.Params]] to configure the `Channel`.
 */
private[netty] class NettyRawServerChannelInitializer(params: Stack.Params)
    extends ChannelInitializer[Channel] {

  import Netty4RawServerChannelInitializer._

  private[this] val Logger(logger) = params[Logger]
  private[this] val Label(label) = params[Label]
  private[this] val Stats(stats) = params[Stats]

  private[this] val channelStatsHandler = None

  private[this] val channelSnooper = None

  override def initChannel(ch: Channel): Unit = {
    // first => last
    // - a request flies from first to last
    // - a response flies from last to first
    //
    // ssl => channel stats => channel snooper => write timeout => read timeout => req stats => ..
    // .. => exceptions => finagle

    val pipeline = ch.pipeline

    channelSnooper.foreach(pipeline.addFirst(ChannelLoggerHandlerKey, _))
    channelStatsHandler.foreach(pipeline.addFirst(ChannelStatsHandlerKey, _))

    // Add SslHandler to the pipeline.
    pipeline.addFirst("tlsInit", new NettyServerSslHandler(params))

    // Enable tracking of the receive buffer sizes (when `poolReceiveBuffers` are enabled).
    if (poolReceiveBuffers()) {
      pipeline.addFirst("receiveBuffersSizeTracker",
        new RecvBufferSizeStatsHandler(stats.scope("transport")))
    }
  }
}
