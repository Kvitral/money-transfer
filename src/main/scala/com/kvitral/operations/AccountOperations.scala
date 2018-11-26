package com.kvitral.operations

import com.kvitral.model.errors.AccountServiceErrors
import com.kvitral.model.{Account, Transaction}

import scala.language.higherKinds

trait AccountOperations[F[_]] {
  def getAccount(i: Long): F[Option[Account]]

  def changeBalance(transaction: Transaction): F[Either[AccountServiceErrors, Unit]]

}
