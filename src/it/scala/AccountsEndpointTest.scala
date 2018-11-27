import java.util.Locale
import java.util.concurrent.ThreadLocalRandom

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.language.postfixOps

class AccountsEndpointTest extends Simulation {

  val httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost:8080")

  val getAccounts: ScenarioBuilder = scenario("GET Accounts Durability")
    .exec(session => session.set("accountId", ThreadLocalRandom.current().nextInt(4) + 1))
    .exec(
      http("accounts")
        .get("/accounts")
        .queryParam("id", "${accountId}")
        .check(jsonPath("$.id").is("${accountId}"))
    )
    .pause(5 second)

  val postTransaction: ScenarioBuilder = scenario("POST Accounts Durability")
    .exec { session =>
      session
        .set("fromAccountId", ThreadLocalRandom.current().nextInt(4) + 1)
        .set("toAccountId", ThreadLocalRandom.current().nextInt(4) + 1)
        .set(
          "amount",
          "%.2f".formatLocal(Locale.ENGLISH, ThreadLocalRandom.current().nextDouble() * 100))
    }
    .exitBlockOnFail(
      exec(
        http("accounts post")
          .post("/accounts")
          .body(StringBody("""
          |{
          |	"from":${fromAccountId},
          |	"to":${toAccountId},
          |	"amount":${amount},
          |	"currency":"RUB"
          |}
        """.stripMargin))
          .asJson
          .check(jsonPath("$.message").is("OK"))
      ).exec(
        http("revert transaction")
          .post("/accounts")
          .body(StringBody("""
          |{
          |	"from":${toAccountId},
          |	"to":${fromAccountId},
          |	"amount":${amount},
          |	"currency":"RUB"
          |}
        """.stripMargin))
          .asJson
          .check(jsonPath("$.message").is("OK"))
      )
    )

  setUp(
    getAccounts.inject((rampUsersPerSec(1) to 50).during(1 minute)),
    postTransaction.inject((rampUsersPerSec(1) to 50).during(1 minute))
  ).protocols(httpProtocol)

}
