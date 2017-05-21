package com.twitter.finagle

import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.toggle.{ StandardToggleMap, ToggleMap }

/**
 * Created by goldratio on 19/05/2017.
 */
package object netty {

  private[finagle] val Toggles: ToggleMap =
    StandardToggleMap("com.twitter.finagle.netty", DefaultStatsReceiver)

}
