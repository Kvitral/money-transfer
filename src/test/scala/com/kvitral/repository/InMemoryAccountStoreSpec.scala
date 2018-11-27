package com.kvitral.repository

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.concurrent.Ref
import com.kvitral.model.errors.{AccountServiceErrors, AccountsAreTheSame, InsufficientBalance}
import com.kvitral.model.{Account, RUB, Transaction}
import com.kvitral.utils.AccountsServiceUtils._
import com.kvitral.utils.TaskRouteTest
import monix.eval.Task
import org.scalatest.{FlatSpec, Matchers}

class InMemoryAccountStoreSpec
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with TaskRouteTest {

  trait mix {

    def inMemoryAccountsStore(
        accountsState: Ref[Task, Map[Long, Account]]): Task[InMemoryAccountStore[Task]] =
      Task.eval(InMemoryAccountStore(accountsState, TaskLogger("InMemoryTest")))

    /*
        using Task.gather which will gives us parallel task execution but will keep order of results
     */

    def concurrentUpdates(inMemoryAccountAlg: InMemoryAccountStore[Task])
      : Task[List[Either[AccountServiceErrors, Unit]]] = {
      val t12 = Transaction(1, 2, 200, RUB)
      val t23 = Transaction(2, 3, 400, RUB)
      val t31 = Transaction(3, 1, 100, RUB)

      val transactionsTasks = List(t12, t23, t31).map(inMemoryAccountAlg.changeBalance)

      Task.gather(transactionsTasks)
    }

    val effectsAfterUpdate = List(Right(()), Left(InsufficientBalance), Right(()))

    val amountsMapAfterUpdates: Map[Long, BigDecimal] = Map(1L -> 400d, 2L -> 300d, 3L -> 100d)
  }

  "InMemoryAccountStore.changeBalance" should "change accounts balances" in new mix {
    val t = Transaction(1, 2, BigDecimal(200.25), RUB)
    runTask(for {
      accountsState <- initialAccountsStateF[Task]
      inmemory <- inMemoryAccountsStore(accountsState)
      res <- inmemory.changeBalance(t)
      afterUpdateState <- accountsState.get
    } yield {
      res shouldEqual Right(())
      afterUpdateState
        .get(t.from) shouldEqual initialAccounts
        .get(t.from)
        .map(a => a.copy(balance = a.balance - t.amount))

      afterUpdateState
        .get(t.to) shouldEqual initialAccounts
        .get(t.to)
        .map(a => a.copy(balance = a.balance + t.amount))
    })
  }

  it should "return InsufficientBalance if account doesn`t have amount on balance" in new mix {
    val transaction = Transaction(1, 2, BigDecimal(600), RUB)
    runTask(for {
      accountsState <- initialAccountsStateF[Task]
      inmemory <- inMemoryAccountsStore(accountsState)
      res <- inmemory.changeBalance(transaction)
      afterUpdateState <- accountsState.get
    } yield {
      res shouldEqual Left(InsufficientBalance)
    })
  }

  it should "return AccountsAreTheSame if both from and to account ids are the same" in new mix {
    val transaction = Transaction(1, 1, BigDecimal(600), RUB)
    runTask(for {
      accountsState <- initialAccountsStateF[Task]
      inmemory <- inMemoryAccountsStore(accountsState)
      res <- inmemory.changeBalance(transaction)
      afterUpdateState <- accountsState.get
    } yield {
      res shouldEqual Left(AccountsAreTheSame)
    })
  }

  it should "handle concurrent updates" in new mix {
    runTask(for {
      store <- initialAccountsStateF[Task]
      inmemory <- inMemoryAccountsStore(store)
      effects <- concurrentUpdates(inmemory)
      afterUpdateState <- store.get
    } yield {
      effects shouldEqual effectsAfterUpdate
      afterUpdateState
        .map { case (k, v) => k -> v.balance } shouldEqual amountsMapAfterUpdates
    })
  }

  "InMemoryAccountStore.getBalance" should "return existing account" in new mix {
    val accNumber = 1L
    runTask(for {
      accountsState <- initialAccountsStateF[Task]
      accountsStore <- inMemoryAccountsStore(accountsState)
      account <- accountsStore.getAccount(accNumber)
    } yield {
      account shouldEqual initialAccounts.get(accNumber)
    })
  }

  it should "return None if account is missing" in new mix {
    val accNumber = 0
    runTask(for {
      accountsState <- initialAccountsStateF[Task]
      accountsStore <- inMemoryAccountsStore(accountsState)
      account <- accountsStore.getAccount(accNumber)
    } yield {
      account shouldEqual None
    })
  }

}
