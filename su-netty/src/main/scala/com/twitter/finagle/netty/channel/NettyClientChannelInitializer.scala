package com.twitter.finagle.netty.channel

/**
  * Created by goldratio on 20/05/2017.
  */
private[netty] object NettyClientChannelInitializer {
  val BufCodecKey = "bufCodec"
  val FramerKey = "framer"
  val WriteTimeoutHandlerKey = "writeTimeout"
  val ReadTimeoutHandlerKey = "readTimeout"
  val ConnectionHandlerKey = "connectionHandler"
  val ChannelStatsHandlerKey = "channelStats"
  val ChannelRequestStatsHandlerKey = "channelRequestStats"
  val ChannelLoggerHandlerKey = "channelLogger"
}
