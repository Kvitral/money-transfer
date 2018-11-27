package com.kvitral.endpoints

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpRequest, MessageEntity}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kvitral.model.errors.AccountNotFound
import com.kvitral.model._
import com.kvitral.repository._
import com.kvitral.services.AccountsService
import com.kvitral.utils.AccountsServiceUtils._
import com.kvitral.utils.TaskRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import monix.eval.Task
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{FlatSpec, Matchers}

class AccountsEndpointSpec
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with TaskRouteTest {

  trait mix {

    def getRoutes: Task[AccountsEndpoint[Task]] =
      for {
        initialAccountStore <- initialAccountsStateF[Task]
        inMemoryLogger = TaskLogger("InMemoryTest")
        accountServiceLogger = TaskLogger("AccountServiceLogger")
        inMemoryAccountStore = InMemoryAccountStore[Task](initialAccountStore, inMemoryLogger)
        accountService = AccountsService[Task](inMemoryAccountStore, accountServiceLogger)
        accountEndpoint = AccountsEndpoint[Task](accountService)
      } yield accountEndpoint
  }

  "GET AccountEndpoint.accounts" should "return existing account" in new mix {
    runTask(
      for {
        acc <- getRoutes
        routes <- acc.accountsRoute

      } yield
        Get("/accounts?id=1") ~> routes ~> check {
          responseAs[Account] shouldEqual Account(1L, 500d, RUB)
        })
  }

  it should "return error message if account is not found" in new mix {
    runTask(
      for {
        acc <- getRoutes
        routes <- acc.accountsRoute
      } yield
        Get("/accounts?id=-1") ~> routes ~> check {
          responseAs[ErrorMessage] shouldEqual ErrorMessage(
            s"Couldn`t find account with id -1",
            AccountNotFound)
        })
  }

  "POST AccountEndpoint.accounts" should "return OK if valid data have been passed" in new mix {
    val transaction = Transaction(1, 2, 100, RUB)
    val transactionEntity: MessageEntity = Marshal(transaction).to[MessageEntity].futureValue
    val request: HttpRequest = Post("/accounts").withEntity(transactionEntity)
    runTask(
      for {
        acc <- getRoutes
        routes <- acc.accountsRoute

      } yield
        request ~> routes ~> check {

          responseAs[SuccessMessage] shouldEqual SuccessMessage("OK")
        })
  }
  it should "return error message if something is wrong" in new mix {
    val transaction = Transaction(-1, 1, 100, RUB)
    val transactionEntity: MessageEntity = Marshal(transaction).to[MessageEntity].futureValue
    val request: HttpRequest = Post("/accounts").withEntity(transactionEntity)
    runTask(
      for {
        acc <- getRoutes
        routes <- acc.accountsRoute

      } yield
        request ~> routes ~> check {

          responseAs[ErrorMessage] shouldEqual ErrorMessage("something went wrong", AccountNotFound)
        })
  }
}
