package com.kvitral

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.ExitCode
import cats.effect.concurrent.Ref
import com.kvitral.endpoints.AccountsEndpoint
import com.kvitral.model.{Account, RUB}
import com.kvitral.repository.{InMemoryAccountStore, TaskLogger}
import com.kvitral.services.AccountsService
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler

import scala.io.StdIn

object Server extends TaskApp {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val scheduler: Scheduler = Scheduler.global

  val initinalAccountsStoreTask: Task[Ref[Task, Map[Long, Account]]] =
    Ref.of(
      Map(
        (1, Account(1L, 500d, RUB)),
        (2, Account(2L, 100d, RUB)),
        (3, Account(3L, 200d, RUB)),
        (4, Account(4L, 300d, RUB)),
        (5, Account(5L, 700d, RUB))
      ))

  def run(args: List[String]): Task[ExitCode] = {

    val appLogger = TaskLogger("Main")

    for {
      _ <- appLogger.info("initializing storage")
      initialAccountStore <- initinalAccountsStoreTask
      _ <- appLogger.info("initializing loggers")
      inMemoryAccountLogger = TaskLogger("InMemoryAccountLogger")
      accountServiceLogger = TaskLogger("AccountService")
      _ <- appLogger.info("initializing repository")
      inMemoryAccountStore = InMemoryAccountStore[Task](initialAccountStore, inMemoryAccountLogger)
      _ <- appLogger.info("initializing services")
      accountService = AccountsService[Task](inMemoryAccountStore, accountServiceLogger)
      _ <- appLogger.info("initializing endpoints")
      accountEndpoint = AccountsEndpoint[Task](accountService)
      _ <- appLogger.info("starting server")
      route <- accountEndpoint.accountsRoute
      _ <- appLogger.info("gettingRoutes")
      _ <- Task.deferFuture(Http().bindAndHandle(route, "localhost", 8080))
      _ <- appLogger.info(
        "to shutdown server hit enter or write something in console and then hit enter :)")
      _ <- Task.delay(StdIn.readLine())
    } yield ExitCode(2)

  }
}
