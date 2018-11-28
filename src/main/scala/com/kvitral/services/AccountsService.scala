package com.kvitral.services

import cats.Monad
import cats.syntax.functor._
import cats.syntax.flatMap._
import com.kvitral.model.errors.{AccountNotFound, AccountServiceErrors}
import com.kvitral.model.{Account, Transaction}
import com.kvitral.operations.{AccountOperations, LoggingOperations}

import scala.language.higherKinds

class AccountsService[F[_]: Monad](accRepo: AccountOperations[F], logger: LoggingOperations[F]) {

  def getAccount(id: Long): F[Either[AccountNotFound.type, Account]] =
    for {
      _ <- logger.info(s"getting account for $id")
      account <- accRepo.getAccount(id)
    } yield account.toRight(AccountNotFound)

  def changeBalance(transaction: Transaction): F[Either[AccountServiceErrors, Unit]] =
    for {
      _ <- logger.info(s"changing balance with transaction $transaction")
      after <- accRepo.changeBalance(transaction)
      _ <- logger.info(s"transaction result is $after")
    } yield after

}

object AccountsService {
  def apply[F[_]: Monad](
      accRepo: AccountOperations[F],
      logger: LoggingOperations[F]): AccountsService[F] =
    new AccountsService(accRepo, logger)
}
