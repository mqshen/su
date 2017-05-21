package com.twitter.finagle.netty

import com.twitter.finagle.server.ServerInfo

/**
 * An experimental option that enables pooling for receive buffers.
 *
 * Since we always copy onto the heap (see `DirectToHeapInboundHandler`), the receive
 * buffers never leave the pipeline hence can safely be pooled.
 * In its current form, this will preallocate at least N * (chunk size) of
 * direct memory at the application startup, where N is the number of worker threads
 * Finagle uses.
 *
 * @note This toggle is only evaluated once, at program startup.
 */
private[netty] object poolReceiveBuffers {
  private[this] lazy val value: Boolean =
    Toggles("com.twitter.finagle.netty.poolReceiveBuffers")(ServerInfo().id.hashCode)

  /**
   * Checks (via a toggle) if pooling of receive buffers is enabled on this instanace.
   */
  def apply(): Boolean = value
}
