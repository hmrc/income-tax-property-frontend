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

package controllers.enhancedstructuresbuildingallowance

import audit.AuditService
import base.SpecBase
import controllers.exceptions.InternalErrorFailure
import forms.enhancedstructuresbuildingallowance.EsbaClaimsFormProvider
import models.PropertyType.toPath
import models.backend.PropertyDetails
import models.{EsbasWithSupportingQuestions, EsbasWithSupportingQuestionsPage, PropertyType, Rentals, RentalsRentARoom, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.prop.TableFor6
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._
import pages.enhancedstructuresbuildingallowance.Esba
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist._
import views.html.enhancedstructuresbuildingallowance.EsbaClaimsView

import scala.concurrent.Future

class EsbaClaimsControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new EsbaClaimsFormProvider()
  val form: Form[Boolean] = formProvider("agent")

  val taxYear = 2024
  val agent = "agent"
  val list: SummaryList = SummaryListViewModel(Seq.empty)

  def onwardRoute: Call = Call("GET", "/foo")

  def rentalsOnwardRouteAddClaim(propertyType: PropertyType): Call =
    Call(
      "GET",
      s"/update-and-submit-income-tax-return/property/$taxYear/${toPath(propertyType)}/enhanced-structures-buildings-allowance/add-claim"
    )

  def onwardRouteNoOtherClaim(propertyType: PropertyType): Call =
    Call(
      "GET",
      s"/update-and-submit-income-tax-return/property/$taxYear/${toPath(propertyType)}/enhanced-structures-buildings-allowance/complete-yes-no"
    )

  def userAnswersWithEsba(propertyType: PropertyType): UserAnswers = emptyUserAnswers
    .set(
      EsbasWithSupportingQuestionsPage(propertyType),
      EsbasWithSupportingQuestions(claimEnhancedStructureBuildingAllowance = true, Some(false), List[Esba]())
    )
    .get

  val scenarios: TableFor6[UserAnswers, String, PropertyType, String, Boolean, Option[String]] =
    Table[UserAnswers, String, PropertyType, String, Boolean, Option[String]](
      ("useranswers", "useranswers definition", "property type", "type definition", "add new claim", "onward route"),
      (
        userAnswersWithEsba(RentalsRentARoom),
        "userAnswersWithEsba",
        RentalsRentARoom,
        "rentalsAndRaR",
        false,
        Some(onwardRouteNoOtherClaim(RentalsRentARoom).url)
      ),
      (
        userAnswersWithEsba(RentalsRentARoom),
        "userAnswersWithEsba",
        RentalsRentARoom,
        "rentalsAndRaR",
        true,
        Some(rentalsOnwardRouteAddClaim(RentalsRentARoom).url)
      ),
      (
        userAnswersWithEsba(Rentals),
        "userAnswersWithEsba",
        Rentals,
        "rentals",
        false,
        Some(onwardRouteNoOtherClaim(Rentals).url)
      ),
      (
        userAnswersWithEsba(Rentals),
        "userAnswersWithEsba",
        Rentals,
        "rentals",
        true,
        Some(rentalsOnwardRouteAddClaim(Rentals).url)
      ),
      (
        emptyUserAnswers,
        "userAnswersWithoutEsba",
        RentalsRentARoom,
        "rentalsAndRaR",
        false,
        None
      ),
      (
        emptyUserAnswers,
        "userAnswersWithoutEsba",
        RentalsRentARoom,
        "rentalsAndRaR",
        true,
        Some(rentalsOnwardRouteAddClaim(RentalsRentARoom).url)
      ),
      (
        emptyUserAnswers,
        "userAnswersWithoutEsba",
        Rentals,
        "rentals",
        false,
        None
      ),
      (
        emptyUserAnswers,
        "userAnswersWithoutEsba",
        Rentals,
        "rentals",
        true,
        Some(rentalsOnwardRouteAddClaim(Rentals).url)
      )
    )
  forAll(scenarios) {
    (
      userAnswers: UserAnswers,
      userAnswersDefinition: String,
      propertyType: PropertyType,
      propertyTypeDefinition: String,
      addNewClaim: Boolean,
      redirectionUrl: Option[String]
    ) =>
      lazy val esbaClaimsRoute: String = routes.EsbaClaimsController.onPageLoad(taxYear, propertyType).url

      s"EsbaClaims Controller with $userAnswersDefinition for $propertyTypeDefinition if add new claim is selected as $addNewClaim" - {

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

          running(application) {
            val request = FakeRequest(GET, esbaClaimsRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[EsbaClaimsView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, list, taxYear, agent, propertyType)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]
          val mockBusinessService = mock[BusinessService]
          val mockAuditService = mock[AuditService]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val cash = false
          when(mockBusinessService.getUkPropertyDetails(any(), any())(any())) thenReturn Future.successful(
            Right(
              Some(
                PropertyDetails(None, None, Some(cash), "incomeSourceId")
              )
            )
          )
          val mockPropertySubmissionService = mock[PropertySubmissionService]
          when(mockPropertySubmissionService.saveJourneyAnswers(any(), any(), any())(any(), any())) thenReturn Future
            .successful(
              Right(())
            )

          val application =
            applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository),
                bind[BusinessService].toInstance(mockBusinessService),
                bind[AuditService].toInstance(mockAuditService),
                bind[PropertySubmissionService].toInstance(mockPropertySubmissionService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, esbaClaimsRoute)
                .withFormUrlEncodedBody(("anotherClaim", addNewClaim.toString))

            val result = route(application, request).value
            redirectionUrl match {
              case Some(url) =>
                redirectLocation(result).value mustEqual url

                whenReady(result) { _ =>
                  val timesForSubmission = if (addNewClaim) 0 else 1
                  verify(mockPropertySubmissionService, times(timesForSubmission))
                    .saveJourneyAnswers(any(), any(), any())(any(), any())
                  verify(mockBusinessService, times(timesForSubmission)).getUkPropertyDetails(any(), any())(any())
                  verify(mockAuditService, times(timesForSubmission)).sendAuditEvent(any())(any(), any())
                }

              case None =>
                await(result.failed) mustEqual
                  InternalErrorFailure(
                    "Enhanced Structure and Building Allowance not found in userAnswers"
                  )

            }

          }

        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

          running(application) {
            val request =
              FakeRequest(POST, esbaClaimsRoute)
                .withFormUrlEncodedBody(("anotherClaim", ""))

            val boundForm = form.bind(Map("anotherClaim" -> ""))

            val view = application.injector.instanceOf[EsbaClaimsView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, list, taxYear, agent, propertyType)(
              request,
              messages(application)
            ).toString
          }
        }

        "must redirect to Journey Recovery for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None, isAgent = true).build()

          running(application) {
            val request = FakeRequest(GET, esbaClaimsRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Journey Recovery for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None, isAgent = true).build()

          running(application) {
            val request =
              FakeRequest(POST, esbaClaimsRoute)
                .withFormUrlEncodedBody(("anotherClaim", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
  }
}
