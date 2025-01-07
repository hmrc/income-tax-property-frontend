/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.ukandforeignproperty

import base.SpecBase
import models.JourneyPath.UkAndForeignPropertyAbout
import models.ukAndForeign.UkAndForeignAbout
import models.{JourneyContext, NormalMode, ReportIncome, TotalPropertyIncome, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.UkForeignPropertyAboutPage
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, contentAsString, defaultAwaitTimeout, redirectLocation, running, status}
import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.ukandforeignproperty.UkAndForeignPropertyCheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UkAndForeignPropertyCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  private val propertySubmissionService = mock[PropertySubmissionService]

  val taxYear: Int = LocalDate.now.getYear
  lazy val UkAndForeignPropertyCheckYourAnswersRoute: String =
    controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear = taxYear, mode = NormalMode).url
  def onwardRoute: Call = Call("GET", "/")

  "UkAndForeignPropertyCheckYourAnswers Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, UkAndForeignPropertyCheckYourAnswersRoute)

        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]
        val view = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        val result = controller.onPageLoad(taxYear, NormalMode)(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, UkAndForeignPropertyCheckYourAnswersRoute)
        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]

        val result = controller.onPageLoad(taxYear, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit and save UkAndForeignAbout" in {

      val ukAndForeignAbout = UkAndForeignAbout(
        totalPropertyIncome = TotalPropertyIncome.values.head,
        reportIncome = Some(ReportIncome.values.head)
      )

      val userAnswers = UserAnswers(userAnswersId)
        .set(UkForeignPropertyAboutPage, ukAndForeignAbout).success.value

      val context = JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = UkAndForeignPropertyAbout)

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), any)(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = true)
        .overrides(inject.bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, UkAndForeignPropertyCheckYourAnswersRoute)
        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]

        val result = controller.onSubmit(taxYear, NormalMode)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear: Int).url

        userAnswers.get(UkForeignPropertyAboutPage) mustBe Some(ukAndForeignAbout)
      }
    }
  }

}
