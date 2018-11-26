package com.kvitral.utils

import cats.effect.Async
import cats.effect.concurrent.Ref
import com.kvitral.model.{Account, RUB}

object AccountsServiceUtils {
  val initialAccounts: Map[Long, Account] = Map(
    (1, Account(1L, 500d, RUB)),
    (2, Account(2L, 100d, RUB)),
    (3, Account(3L, 200d, RUB))
  )

  def initialAccountsStateF[F[_]: Async]: F[Ref[F, Map[Long, Account]]] =
    Ref.of[F, Map[Long, Account]](initialAccounts)

}
