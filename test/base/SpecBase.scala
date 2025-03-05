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

package base

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import models.backend.PropertyDetails
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import service.{BusinessService, PropertySubmissionService}

import java.time.LocalDate

trait SpecBase
    extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience {

  val userAnswersId: String = "id"

  val foreignPropertyDetails: PropertyDetails =
    PropertyDetails(Some("foreign-property"), Some(LocalDate.now()), Some(true), "some-id")

  val propertySubmissionService: PropertySubmissionService = mock[PropertySubmissionService]
  val businessService: BusinessService = mock[BusinessService]
  val audit: AuditService = mock[AuditService]
  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def config(app: Application): FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  def definedMessages(app: Application): Map[String, Map[String, String]] =
    app.injector.instanceOf[MessagesApi].messages

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None,
    isAgent: Boolean
  ): GuiceApplicationBuilder = {
    val fakeIdentifierAction =
      if (isAgent) {
        bind[IdentifierAction].to[FakeAgentIdentifierAction]
      } else {
        bind[IdentifierAction].to[FakeIndividualIdentifierAction]
      }

    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        fakeIdentifierAction,
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
  }
}
