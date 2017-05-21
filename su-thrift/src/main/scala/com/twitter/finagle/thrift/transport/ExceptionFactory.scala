package com.twitter.finagle.thrift.transport

/**
  * Created by goldratio on 20/05/2017.
  */
private[transport] object ExceptionFactory {
  def wrongServerWriteType(value: Any): Exception = {
    val msg = s"Expected an `Array[Byte]` but received ${value.getClass.getSimpleName}"
    new IllegalArgumentException(msg)
  }

  def wrongClientWriteType(value: Any): Exception = {
    val msg = s"Expected a ThriftClientRequest but received ${value.getClass.getSimpleName}"
    new IllegalArgumentException(msg)
  }
}
