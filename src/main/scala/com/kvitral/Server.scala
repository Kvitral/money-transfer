package com.kvitral

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.concurrent.Ref
import com.kvitral.endpoints.AccountsEndpoint
import com.kvitral.model.{Account, RUB}
import com.kvitral.repository.{InMemoryAccountStore, TaskLogger}
import com.kvitral.services.AccountsService
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Promise}

object Server {
  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  val initinalAccountsStoreTask: Task[Ref[Task, Map[Long, Account]]] =
    Ref.of(
      Map(
        (1, Account(1L, 500d, RUB)),
        (2, Account(2L, 100d, RUB)),
        (3, Account(3L, 200d, RUB)),
        (4, Account(4L, 300d, RUB)),
        (5, Account(5L, 700d, RUB))
      ))

  def main(args: Array[String]): Unit = {

    val appLogger = TaskLogger("Main")

    val program = for {
      _ <- appLogger.info("initializing storage:")
      initialAccountStore <- initinalAccountsStoreTask
      _ <- appLogger.info("initializing loggers:")
      inMemoryAccountLogger = TaskLogger("InMemoryAccountLogger")
      accountServiceLogger = TaskLogger("AccountService")
      _ <- appLogger.info("initializing algebras:")
      inMemoryAccountStore = InMemoryAccountStore[Task](initialAccountStore, inMemoryAccountLogger)
      _ <- appLogger.info("initializing services:")
      accountService = AccountsService[Task](inMemoryAccountStore, accountServiceLogger)
      accountEndpoint = AccountsEndpoint[Task](accountService)
      _ <- appLogger.info("starting server")
      route <- accountEndpoint.accountsRoute
      _ <- appLogger.info("gettingRoutes")
      _ <- Task.deferFuture(Http().bindAndHandle(route, "localhost", 8080))
    } yield ()

    for (_ <- program.runToFuture)
      appLogger.info("serverStarted").runAsyncAndForget

    val promise = Promise[Unit]

    Await.result(promise.future, Duration.Inf)

  }
}
