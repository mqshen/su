package com.twitter.finagle.netty

import com.twitter.app.GlobalFlag
import com.twitter.jvm.numProcs

/**
 * Created by goldratio on 19/05/2017.
 */
object numWorkers extends GlobalFlag((numProcs() * 2).ceil.toInt, "number of netty4 worker threads")
