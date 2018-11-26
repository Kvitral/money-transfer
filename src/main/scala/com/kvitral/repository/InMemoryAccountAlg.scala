package com.kvitral.repository

import cats.Monad
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.kvitral.algebras.{AccountAlg, Logging}
import com.kvitral.model.errors.{AccountNotFound, AccountServiceErrors, InsufficientBalance}
import com.kvitral.model.{Account, Transaction}

import scala.language.higherKinds

class InMemoryAccountAlg[F[_]: Monad](accountState: Ref[F, Map[Long, Account]], logger: Logging[F])
    extends AccountAlg[F] {

  override def getAccount(i: Long): F[Option[Account]] =
    for {
      account <- accountState.get
    } yield account.get(i)

  override def changeBalance(transaction: Transaction): F[Either[AccountServiceErrors, Unit]] =
    for {
      trResult <- accountState.modify(state => performTransaction(transaction, state))
      _ <- logger.info(s"result of transaction is ${trResult.toString}")
    } yield trResult

  private def performTransaction(
      t: Transaction,
      state: Map[Long, Account]): (Map[Long, Account], Either[AccountServiceErrors, Unit]) = {

    val finishedTransaction = for {
      accountFrom <- Either
        .fromOption(state.get(t.from), AccountNotFound)
        .filterOrElse(_.balance >= t.amount, InsufficientBalance)

      accountTo <- Either.fromOption(state.get(t.to), AccountNotFound)
    } yield {
      state
        .updated(accountFrom.id, accountFrom.copy(balance = accountFrom.balance - t.amount))
        .updated(accountTo.id, accountTo.copy(balance = accountTo.balance + t.amount))
    }

    finishedTransaction match {
      case Left(error) => (state, Left(error))
      case Right(updatedState) => (updatedState, Right(()))
    }
  }

}

object InMemoryAccountAlg {
  def apply[F[_]: Monad](
      accountState: Ref[F, Map[Long, Account]],
      logger: Logging[F]): InMemoryAccountAlg[F] =
    new InMemoryAccountAlg(accountState, logger)
}
