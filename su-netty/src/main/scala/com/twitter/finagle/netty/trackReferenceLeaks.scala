package com.twitter.finagle.netty

import java.util.concurrent.atomic.AtomicInteger

import com.twitter.finagle.server.ServerInfo
import com.twitter.finagle.stats.FinagleStatsReceiver
import com.twitter.finagle.toggle.Toggle
import io.netty.util.{ ResourceLeakDetector, ResourceLeakDetectorFactory }

/**
 * Created by goldratio on 19/05/2017.
 */
object trackReferenceLeaks {

  private[this] val underlying: Toggle[Int] =
    Toggles("com.twitter.finagle.netty.EnableReferenceLeakTracking")

  private[netty] lazy val enabled: Boolean = underlying(ServerInfo().id.hashCode)

  private[netty] def leaksDetected(): Int = leakCnt.get()

  private[this] val referenceLeaks = FinagleStatsReceiver.scope("netty").counter("reference_leaks")

  private[this] val leakCnt: AtomicInteger = new AtomicInteger(0)

  private[netty] lazy val init: Unit = {
    if (enabled) {
      if (ResourceLeakDetector.getLevel == ResourceLeakDetector.Level.DISABLED)
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE)

      ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(
        new StatsLeakDetectorFactory({ () =>
          referenceLeaks.incr()
          leakCnt.incrementAndGet()
        }))
    }
  }

}
