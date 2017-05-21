package com.twitter.finagle.netty.param

import com.twitter.finagle.Stack
import com.twitter.finagle.netty.{ LeakDetectingAllocator, trackReferenceLeaks, usePooling }
import io.netty.buffer.{ ByteBufAllocator, PooledByteBufAllocator, UnpooledByteBufAllocator }

private[netty] case class Allocator(allocator: ByteBufAllocator)

private[netty] object Allocator {

  // nb: we can't use io.netty.buffer.UnpooledByteBufAllocator.DEFAULT
  //     because we don't prefer direct byte buffers.
  //
  // See CSL-3027 for more details.
  val Unpooled =
    if (trackReferenceLeaks.enabled) LeakDetectingAllocator
    else new UnpooledByteBufAllocator( /* preferDirect */ false, /* disableLeakDetector */ true)

  implicit val allocatorParam: Stack.Param[Allocator] = Stack.Param(Allocator(
    if (usePooling()) PooledByteBufAllocator.DEFAULT else Unpooled))
}
