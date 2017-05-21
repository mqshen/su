package com.twitter.finagle.netty.channel

import com.twitter.finagle.transport.Transport
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{Channel, ChannelInitializer}

/**
  * Created by goldratio on 20/05/2017.
  */
@Sharable
private[netty] class ServerBridge[In, Out](
                                             transportFac: Channel => Transport[In, Out],
                                             serveTransport: Transport[In, Out] => Unit)
  extends ChannelInitializer[Channel] {

  def initChannel(ch: Channel): Unit = {
    val transport: Transport[In, Out] = transportFac(ch)
    serveTransport(transport)
  }
}
