package com.twitter.finagle.thrift.transport.netty

import java.net.SocketAddress

import com.twitter.finagle.client.Transporter
import com.twitter.finagle.netty.{NettyListener, NettyTransporter}
import com.twitter.finagle.param.Label
import com.twitter.finagle.server.Listener
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.{Stack, Thrift}
import io.netty.channel.ChannelPipeline

/**
  * Created by goldratio on 20/05/2017.
  */
object NettyTransport {

  val ClientPipelineInit: Stack.Params => ChannelPipeline => Unit =
  { params: Stack.Params =>
  { pipeline: ChannelPipeline =>
    addFramerAtLast(pipeline, params)
    pipeline.addLast("clientByteCodec", ClientByteBufCodec())
    ()
  }
  }


  val Client: Stack.Params => SocketAddress => Transporter[ThriftClientRequest, Array[Byte]] = { params =>
    NettyTransporter.raw(ClientPipelineInit(params), _, params)
  }

  val ServerPipelineInit: Stack.Params => ChannelPipeline => Unit =
  { params =>
  { pipeline: ChannelPipeline =>
    addFramerAtLast(pipeline, params)
    pipeline.addLast("serverByteCodec", ServerByteBufCodec())
    ()
  }
  }

  val Server: Stack.Params => Listener[Array[Byte], Array[Byte]] = { params =>
    NettyListener[Array[Byte], Array[Byte]](ServerPipelineInit(params),
      if (params.contains[Label]) params else params + Label("thrift"))
  }


  // Add a framed codec or buffered decoded based on the provided stack params
  private def addFramerAtLast(pipeline: ChannelPipeline, params: Stack.Params): Unit = {
    val Thrift.param.Framed(framed) = params[Thrift.param.Framed]
    if (framed) {
      pipeline.addLast("thriftFrameCodec", ThriftFrameCodec())
    } else {
      // use the buffered transport framer
      val Thrift.param.ProtocolFactory(protocolFactory) = params[Thrift.param.ProtocolFactory]
      pipeline.addLast("thriftBufferDecoder", new ThriftBufferedTransportDecoder(protocolFactory))
    }
  }
}
