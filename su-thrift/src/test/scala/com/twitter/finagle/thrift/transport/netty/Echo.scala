package com.twitter.finagle.thrift.transport.netty

import java.net.SocketAddress
import java.nio.charset.StandardCharsets.UTF_8

import com.twitter.finagle._
import com.twitter.finagle.client.{StackClient, StdStackClient, Transporter}
import com.twitter.finagle.dispatch.{SerialClientDispatcher, SerialServerDispatcher}
import com.twitter.finagle.netty.{NettyListener, NettyTransporter}
import com.twitter.finagle.server.{Listener, StackServer, StdStackServer}
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Await, Future}
import io.netty.channel.ChannelPipeline
import io.netty.handler.codec.{DelimiterBasedFrameDecoder, Delimiters}
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}
import io.netty.util.CharsetUtil

/**
  * Created by goldratio on 20/05/2017.
  */

object Echo extends Client[String, String] with Server[String, String] {
  //#client
  case class Client(
                     stack: Stack[ServiceFactory[String, String]] = StackClient.newStack,
                     params: Stack.Params = StackClient.defaultParams
                   ) extends StdStackClient[String, String, Client] {
    protected type In = String
    protected type Out = String

    protected def copy1( stack: Stack[ServiceFactory[In, Out]], params: Stack.Params): Client = copy(stack, params)

    val ClientPipelineInit: ChannelPipeline => Unit = { pipeline: ChannelPipeline =>
      pipeline.addLast("line", new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter(): _*))
      pipeline.addLast("stringDecoder", new StringDecoder(UTF_8))
      pipeline.addLast("stringEncoder", new StringEncoder(UTF_8))
    }
    //#transporter
    protected def newTransporter(addr: SocketAddress): Transporter[In, Out] =
      NettyTransporter.raw(ClientPipelineInit, addr, params)

    protected def newDispatcher(transport: Transport[In, Out]): Service[In, Out] = new SerialClientDispatcher(transport)

  }
  //#client

  val client = Client()

  def newService(dest: Name, label: String): Service[String, String] =
    client.newService(dest, label)

  def newClient(dest: Name, label: String): ServiceFactory[String, String] =
    client.newClient(dest, label)


  //#server
  case class Server( stack: Stack[ServiceFactory[String, String]] = StackServer.newStack,
                     params: Stack.Params = StackServer.defaultParams ) extends StdStackServer[String, String, Server] {
    protected type In = String
    protected type Out = String

    protected def copy1( stack: Stack[ServiceFactory[String, String]] = this.stack,
                         params: Stack.Params = this.params ): Server = copy(stack, params)

    val StringServerPipeline: ChannelPipeline => Unit = {
      pipeline: ChannelPipeline =>
      pipeline.addLast("serverByteCodec", ServerByteBufCodec())
      ()
    }

    //#serverlistener
    protected def newListener(): Listener[String, String] = NettyListener(StringServerPipeline, params)

    protected def newDispatcher(transport: Transport[String, String],
                                service: Service[String, String]) =
      new SerialServerDispatcher(transport, service)
  }
  //#server

  val server = Server()

  def serve(addr: SocketAddress,
            service: ServiceFactory[String, String]): ListeningServer =
    server.serve(addr, service)
}

object EchoServerExample {
  def main(args: Array[String]): Unit = {
    //#serveruse
    val service = new Service[String, String] {
      def apply(request: String) = Future.value(request)
    }
    val server = Echo.serve(":8080", service)
    Await.result(server)
    //#serveruse
  }
}

object BasicClientExample {
  def main(args: Array[String]): Unit = {
    val server = Echo.newService(":8080", "test")
    val result = server("sssss")
    val t = Await.result(result)
    println(t)
  }
}