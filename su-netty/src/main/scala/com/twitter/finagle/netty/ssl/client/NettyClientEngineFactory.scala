package com.twitter.finagle.netty.ssl.client

import javax.net.ssl.SSLEngine

import com.twitter.finagle.Address
import com.twitter.finagle.netty.param.Allocator
import com.twitter.finagle.netty.ssl.NettySslConfigurations
import com.twitter.finagle.ssl._
import com.twitter.finagle.ssl.client.{SslClientConfiguration, SslClientEngineFactory}
import io.netty.buffer.ByteBufAllocator
import io.netty.handler.ssl.{OpenSsl, SslContext, SslContextBuilder}

/**
 * This engine factory uses Netty 4's `SslContextBuilder`. It is the
 * recommended path for using native SSL/TLS engines with Finagle.
 */
class NettyClientEngineFactory(allocator: ByteBufAllocator, forceJdk: Boolean)
  extends SslClientEngineFactory {

  private[this] def mkSslEngine(
    context: SslContext,
    address: Address,
    config: SslClientConfiguration
  ): SSLEngine =
    address match {
      case Address.Inet(isa, _) =>
        context.newEngine(allocator, SslClientEngineFactory.getHostname(isa, config), isa.getPort)
      case _ =>
        context.newEngine(allocator)
    }

  private[this] def addKey(
    builder: SslContextBuilder,
    keyCredentials: KeyCredentials
  ): SslContextBuilder =
    keyCredentials match {
      case KeyCredentials.Unspecified =>
        builder // Do Nothing
      case KeyCredentials.CertAndKey(certFile, keyFile) =>
        builder.keyManager(certFile, keyFile)
      case _: KeyCredentials.CertKeyAndChain =>
        throw SslConfigurationException.notSupported(
          "KeyCredentials.CertKeyAndChain", "Netty4ClientEngineFactory")
    }

  /**
   * Creates a new `Engine` based on an `Address` and an `SslClientConfiguration`.
   *
   * @param address A physical address which potentially includes metadata.
   *
   * @param config A collection of parameters which the engine factory should
   * consider when creating the TLS client `Engine`.
   *
   * @note Using `TrustCredentials.Insecure` forces the underlying engine to be
   * a JDK engine and not a native engine, based on what Netty supports.
   *
   * @note `ApplicationProtocols` other than Unspecified are only supported
   * by using a native engine via netty-tcnative.
   */
  def apply(address: Address, config: SslClientConfiguration): Engine = {
    val builder = SslContextBuilder.forClient()
    val withKey = addKey(builder, config.keyCredentials)
    val withProvider = NettySslConfigurations.configureProvider(withKey, forceJdk)
    val withTrust = NettySslConfigurations.configureTrust(withProvider, config.trustCredentials)
    val withAppProtocols = NettySslConfigurations.configureApplicationProtocols(
      withTrust, config.applicationProtocols)
    val context = withAppProtocols.build()
    val engine = new Engine(mkSslEngine(context, address, config))
    SslClientEngineFactory.configureEngine(engine, config)
    engine
  }

}

object NettyClientEngineFactory {

  /**
   * Creates an instance of the [[NettyClientEngineFactory]] using the
   * allocator defined for use by default in Finagle-Netty4.
   *
   * @param forceJdk Indicates whether the underlying `SslProvider` should
   * be forced to be the Jdk version instead of the native version if
   * available.
   */
  def apply(forceJdk: Boolean): NettyClientEngineFactory = {
    val allocator = Allocator.allocatorParam.default.allocator
    new NettyClientEngineFactory(allocator, forceJdk)
  }

  /**
   * Creates an instance of the [[NettyClientEngineFactory]] using the
   * specified allocator.
   *
   * @param allocator The allocator which should be used as part of
   * `Engine` creation. See Netty's `SslContextBuilder` docs for
   * more information.
   *
   * @note Whether this engine factory should be forced to use the
   * Jdk version is determined by whether Netty is able to load
   * a native engine library via netty-tcnative.
   */
  def apply(allocator: ByteBufAllocator): NettyClientEngineFactory =
    new NettyClientEngineFactory(allocator, !OpenSsl.isAvailable)

  /**
   * Creates an instance of the [[NettyClientEngineFactory]] using the
   * default allocator.
   *
   * @note Whether this engine factory should be forced to use the
   * Jdk version is determined by whether Netty is able to load
   * a native engine library via netty-tcnative.
   */
  def apply(): NettyClientEngineFactory =
    apply(!OpenSsl.isAvailable)

}
