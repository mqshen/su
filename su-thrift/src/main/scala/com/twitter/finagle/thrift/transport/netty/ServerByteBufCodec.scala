package com.twitter.finagle.thrift.transport.netty

import com.twitter.finagle.thrift.transport.ExceptionFactory
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel._
/**
  * Created by goldratio on 20/05/2017.
  */
private[netty] object ServerByteBufCodec {

  def apply(): ChannelHandler = {
    val encoder = ThriftServerArrayToByteBufEncoder
    val decoder = ThriftByteBufToArrayDecoder
    new CombinedChannelDuplexHandler(decoder, encoder)
  }

  @Sharable
  private object ThriftServerArrayToByteBufEncoder extends ChannelOutboundHandlerAdapter {
    override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit =
      msg match {
        case array: Array[Byte] =>
          val buf = Unpooled.wrappedBuffer(array)
          ctx.writeAndFlush(buf, promise)

        case other =>
          val ex = ExceptionFactory.wrongServerWriteType(other)
          promise.setFailure(ex)
          throw ex
      }
  }
}
