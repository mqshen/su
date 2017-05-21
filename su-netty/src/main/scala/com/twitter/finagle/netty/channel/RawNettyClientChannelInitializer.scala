package com.twitter.finagle.netty.channel

import com.twitter.finagle.Stack
import io.netty.channel.{Channel, ChannelPipeline}

/**
  * Created by goldratio on 20/05/2017.
  */
private[netty] class RawNettyClientChannelInitializer(
                                                         pipelineInit: ChannelPipeline => Unit,
                                                         params: Stack.Params)
  extends AbstractNettyClientChannelInitializer(params) {

  override def initChannel(ch: Channel): Unit = {
    super.initChannel(ch)
    pipelineInit(ch.pipeline)
  }
}
