package com.kvitral.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.effect.concurrent.Ref
import com.kvitral.model.errors.AccountNotFound
import com.kvitral.model.{Account, RUB}
import com.kvitral.repository.{InMemoryAccountStore, TaskLogger}
import com.kvitral.utils.AccountsServiceUtils._
import com.kvitral.utils.TaskRouteTest
import monix.eval.Task
import org.scalatest.{FlatSpec, Matchers}

class AccountsServiceSpec
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with TaskRouteTest {

  trait mix {

    def getAccountService(store: Ref[Task, Map[Long, Account]]): Task[AccountsService[Task]] =
      for {
        _ <- Task.unit
        inMemoryLogger = TaskLogger("InMemoryTest")
        accServiceLogger = TaskLogger("AccountServiceLogger")
        accAlg = InMemoryAccountStore[Task](store, inMemoryLogger)
      } yield AccountsService(accAlg, accServiceLogger)

  }

  "AccountsServiceSpec.getAccount" should "return account" in new mix {
    val id = 1
    runTask(for {
      store <- initialAccountsStateF[Task]
      service <- getAccountService(store)
      account <- service.getAccount(id)
    } yield {
      account shouldEqual Right(Account(1L, 500d, RUB))
    })
  }

  it should "return AccountNotFound exception if account is missing" in new mix {
    val id: Long = -1
    runTask(for {
      store <- initialAccountsStateF[Task]
      service <- getAccountService(store)
      account <- service.getAccount(id)
    } yield {
      account shouldEqual Left(AccountNotFound)
    })
  }
}
