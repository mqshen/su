package com.twitter.finagle.netty

import com.twitter.finagle.server.ServerInfo

/**
 * An experimental option that enables Netty 4 pooling. When enabled, default pooled
 * allocator (i.e., `io.netty.buffer.PooledByteBufAllocator.DEFAULT`) will be used in
 * each Netty channel created by Finagle.
 *
 * An essential pooling configuration parameter is a chunk size implying a trade-off
 * between an initial memory footprint and a maximum size of the buffer that can be
 * pooled. By default, chunk size is 1mb and could be overridden with the
 * `io.netty.allocator.maxOrder` JVM system property.
 *
 * @note This toggle is only evaluated once, at program startup.
 */
object usePooling {
  private[this] lazy val value: Boolean =
    Toggles("com.twitter.finagle.netty.UsePooling")(ServerInfo().id.hashCode)

  /**
   * Checks (via a toggle) if pooling is enabled on this instance.
   */
  def apply(): Boolean = value
}
