/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, stubFor, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import config.FrontendAppConfig
import handlers.ErrorHandler
import models.User
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.HeaderNames
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext}

trait IntegrationTest extends AnyWordSpecLike with Matchers with GuiceOneServerPerSuite with WireMockSupport with OptionValues {

  val authorizationHeader: (String, String) = HeaderNames.AUTHORIZATION -> "mock-bearer-token"
  private val dateNow: LocalDate = LocalDate.now()
  private val taxYearCutoffDate: LocalDate = LocalDate.parse(s"${dateNow.getYear}-04-05")

  val taxYear: Int = if (dateNow.isAfter(taxYearCutoffDate)) LocalDate.now().getYear + 1 else LocalDate.now().getYear
  val taxYearEOY: Int = taxYear - 1
  val taxYearEndOfYearMinusOne: Int = taxYearEOY - 1

  val validTaxYearList: Seq[Int] = Seq(taxYearEOY - 1, taxYearEOY, taxYear)
  val singleValidTaxYear: Seq[Int] = Seq(taxYearEndOfYearMinusOne)

  val nino = "AA123456A"
  val mtditid = "1234567890"
  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val affinityGroup = "Individual"

  val xSessionId: (String, String) = "X-Session-ID" -> sessionId
  val csrfContent: (String, String) = "Csrf-Token" -> "nocheck"

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit lazy val messages: Messages = messagesApi.preferred(FakeRequest())
  lazy val welshMessages: Messages = messagesApi.preferred(Seq(Lang("cy")))

  implicit lazy val user: User = User(mtditid, nino, affinityGroup, None)
  implicit val correlationId: String = UUID.randomUUID().toString

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier().withExtraHeaders("mtditid" -> mtditid)

  implicit def wsClient: WSClient = app.injector.instanceOf[WSClient]

  val appUrl = s"http://localhost:$port/update-and-submit-income-tax-return/personal-income"

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  lazy val commonConfig: Map[String, Any] = Map(
    "auditing.enabled" -> false,
    "metrics.enabled" -> false,
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.income-tax-submission-frontend.url" -> s"http://$wireMockHost:$wireMockPort",
    "microservice.services.income-tax-property.url" -> s"http://$wireMockHost:$wireMockPort",
    "microservice.services.auth.host" -> wireMockHost,
    "microservice.services.auth.port" -> wireMockPort,
    "microservice.services.income-tax-session-data.url" -> s"http://$wireMockHost:$wireMockPort",
    "microservice.services.feedback-frontend.url" -> s"http://$wireMockHost:$wireMockPort",
    "microservice.services.view-and-change" -> s"http://$wireMockHost:$wireMockPort",
    "urls.login" -> s"/auth-login-stub/gg-sign-in"
  )

  def buildApplication(welshEnabled: Boolean = true): Application =
    GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .configure(
        config(welshEnabled)
      )
      .build()

  def config(welshEnabled: Boolean = true): Map[String, Any] = commonConfig ++ Map(
    "feature-switch.welshToggleEnabled" -> welshEnabled
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config())
    .build()

  implicit lazy val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  lazy val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  def stubGet(url: String, status: Integer, body: String): StubMapping =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(body)
      )
    )
}
