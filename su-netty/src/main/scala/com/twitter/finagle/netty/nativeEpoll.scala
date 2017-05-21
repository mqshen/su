package com.twitter.finagle.netty

import com.twitter.finagle.server.ServerInfo
import com.twitter.finagle.toggle.Toggle
import io.netty.channel.epoll.Epoll

/**
 * Created by goldratio on 19/05/2017.
 */
private[netty] object nativeEpoll {

  private[this] val underlying: Toggle[Int] =
    Toggles("com.twitter.finagle.netty.UseNativeEpoll")

  // evaluated once per VM for consistency between listeners, transporters + worker pool.
  lazy val enabled: Boolean = underlying(ServerInfo().id.hashCode) && Epoll.isAvailable

}
