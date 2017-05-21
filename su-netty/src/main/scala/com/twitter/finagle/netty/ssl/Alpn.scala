package com.twitter.finagle.netty.ssl

import com.twitter.finagle.Stack
import com.twitter.finagle.ssl.ApplicationProtocols

/**
 * Created by goldratio on 19/05/2017.
 */
private[finagle] case class Alpn(protocols: ApplicationProtocols)

private[finagle] object Alpn {
  implicit val param = Stack.Param(Alpn(ApplicationProtocols.Unspecified))
}
