package com.twitter.finagle.server

import java.nio.charset.StandardCharsets

import io.netty.channel._

/**
  * Created by goldratio on 20/05/2017.
  */

//
//
//private[finagle] abstract class StringServerPipeline(needsBufCodec: Boolean) extends (ChannelPipeline => Unit) {
//
//  private val maxFrameLength = 0x7FFFFFFF
//  private val lengthFieldOffset = 0
//  private val lengthFieldLength = 4
//  private val lengthAdjustment = 0
//  private val initialBytesToStrip = 4
//
//  def bufferManagerName: String
//  def bufferManager: ChannelHandler
//
//  def apply(pipeline: ChannelPipeline): Unit = {
//    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
//      maxFrameLength,
//      lengthFieldOffset,
//      lengthFieldLength,
//      lengthAdjustment,
//      initialBytesToStrip))
//    pipeline.addLast("frameEncoder", new LengthFieldPrepender(lengthFieldLength))
//    pipeline.addLast(bufferManagerName, bufferManager)
//    if (needsBufCodec) pipeline.addLast("bufCodec", BufCodec)
//  }
//}
///**
//  * A mux framer which copies all inbound direct buffers onto the heap.
//  */
//private[finagle] object StringServerPipeline extends StringServerPipeline(true) {
//  def bufferManager: ChannelHandler = AnyToHeapInboundHandler
//  def bufferManagerName: String = AnyToHeapInboundHandlerName
//}
