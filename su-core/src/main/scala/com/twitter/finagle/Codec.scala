package com.twitter.finagle

import com.twitter.finagle.dispatch.{ GenSerialClientDispatcher, SerialClientDispatcher, SerialServerDispatcher }
import com.twitter.finagle.tracing.TraceInitializerFilter
import com.twitter.finagle.transport.Transport
import com.twitter.util.Closable
import java.net.{ InetSocketAddress, SocketAddress }

/**
 * Codecs provide protocol encoding and decoding via netty pipelines
 * as well as a standard filter stack that is applied to services
 * from this codec.
 */
trait Codec[Req, Rep] {
  /**
   * The pipeline factory that implements the protocol.
   */
  /* Note: all of the below interfaces are scheduled for deprecation in favor of
   * clients/servers
   */

  /**
   * Prepare a factory for usage with the codec. Used to allow codec
   * modifications to the service at the top of the network stack.
   */
  def prepareServiceFactory(
    underlying: ServiceFactory[Req, Rep]): ServiceFactory[Req, Rep] =
    underlying

  /**
   * Prepare a connection factory. Used to allow codec modifications
   * to the service at the bottom of the stack (connection level).
   */
  final def prepareConnFactory(underlying: ServiceFactory[Req, Rep]): ServiceFactory[Req, Rep] =
    prepareConnFactory(underlying, Stack.Params.empty)

  def prepareConnFactory(
    underlying: ServiceFactory[Req, Rep],
    params: Stack.Params): ServiceFactory[Req, Rep] = underlying

  final def newClientDispatcher(transport: Transport[Any, Any]): Service[Req, Rep] =
    newClientDispatcher(transport, Stack.Params.empty)

  def newClientDispatcher(
    transport: Transport[Any, Any],
    params: Stack.Params): Service[Req, Rep] = {
    // In order to not break the Netty 3 API, we provide some 'alternative facts'
    // and continue without our dynamic check
    val clazz = classOf[Any].asInstanceOf[Class[Rep]]
    new SerialClientDispatcher(
      Transport.cast[Req, Rep](clazz, transport),
      params[param.Stats].statsReceiver.scope(GenSerialClientDispatcher.StatsScope))
  }

  def newServerDispatcher(
    transport: Transport[Any, Any],
    service: Service[Req, Rep]): Closable = {
    // In order to not break the Netty 3 API, we provide some 'alternative facts'
    // and continue without our dynamic check
    val clazz = classOf[Any].asInstanceOf[Class[Req]]
    new SerialServerDispatcher[Req, Rep](Transport.cast[Rep, Req](clazz, transport), service)
  }

  /**
   * Is this Codec OK for failfast? This is a temporary hack to
   * disable failFast for codecs for which it isn't well-behaved.
   */
  def failFastOk = true

  /**
   * A hack to allow for overriding the TraceInitializerFilter when using
   * Client/Server Builders rather than stacks.
   */
  def newTraceInitializer: Stackable[ServiceFactory[Req, Rep]] = TraceInitializerFilter.clientModule[Req, Rep]

  /**
   * A protocol library name to use for displaying which protocol library this client or server is using.
   */
  def protocolLibraryName: String = "not-specified"
}

/**
 * An abstract class version of the above for java compatibility.
 */
abstract class AbstractCodec[Req, Rep] extends Codec[Req, Rep]

/**
 * Codec factories create codecs given some configuration.
 */

/**
 * Clients
 */
case class ClientCodecConfig(serviceName: String)

/**
 * Servers
 */
case class ServerCodecConfig(serviceName: String, boundAddress: SocketAddress) {
  def boundInetSocketAddress = boundAddress match {
    case ia: InetSocketAddress => ia
    case _                     => new InetSocketAddress(0)
  }
}

/**
 * A combined codec factory provides both client and server codec
 * factories in one (when available).
 */
trait CodecFactory[Req, Rep] {
  type Client = ClientCodecConfig => Codec[Req, Rep]
  type Server = ServerCodecConfig => Codec[Req, Rep]

  def client: Client
  def server: Server

  /**
   * A protocol library name to use for displaying which protocol library this client or server is using.
   */
  def protocolLibraryName: String = "not-specified"
}
