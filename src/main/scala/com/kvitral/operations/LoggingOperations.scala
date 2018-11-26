package com.kvitral.operations

trait LoggingOperations[F[_]] {
  def info(msg: => String): F[Unit]

  def error(msg: => String): F[Unit]
}
