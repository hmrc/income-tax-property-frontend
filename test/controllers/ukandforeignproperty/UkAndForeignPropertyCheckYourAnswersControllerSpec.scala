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
import controllers.exceptions.NotFoundException
import models.ReportIncome.DoNoWantToReport
import models.TotalPropertyIncome.LessThan
import models.ukAndForeign.UkAndForeignAbout
import models.{JourneyContext, JourneyPath, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.{ReportIncomePage, TotalPropertyIncomePage}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
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
    controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear = taxYear).url

  "UkAndForeignPropertyCheckYourAnswers Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, UkAndForeignPropertyCheckYourAnswersRoute)

        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]
        val view = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        val result = controller.onPageLoad(taxYear)(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, UkAndForeignPropertyCheckYourAnswersRoute)
        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]

        val result = controller.onPageLoad(taxYear)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a POST (onSubmit)" in {

      val userAnswers = UserAnswers("test").set(TotalPropertyIncomePage, LessThan).get
      val updated = userAnswers.set(ReportIncomePage, DoNoWantToReport).get

      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = JourneyPath.UkAndForeignPropertyAbout)
      val ukAndForeignAbout = UkAndForeignAbout(totalPropertyIncome = LessThan, reportIncome = Some(DoNoWantToReport))

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), ArgumentMatchers.eq(ukAndForeignAbout))(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = Some(updated), isAgent = false)
        .overrides(inject.bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, UkAndForeignPropertyCheckYourAnswersRoute)
        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]

        val result = controller.onSubmit(taxYear)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear: Int).url
      }
    }

    "must return NotFoundException when UkAndForeignAbout section is missing in userAnswers" in {

      val userAnswers = UserAnswers("test")

      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = JourneyPath.UkAndForeignPropertyAbout)
      val ukAndForeignAbout = UkAndForeignAbout(totalPropertyIncome = LessThan, reportIncome = Some(DoNoWantToReport))

      when(
        propertySubmissionService
          .saveJourneyAnswers(ArgumentMatchers.eq(context), ArgumentMatchers.eq(ukAndForeignAbout))(
            any(),
            any()
          )
      ) thenReturn Future(Right())

      val application = applicationBuilder(userAnswers = Some(userAnswers), isAgent = false)
        .overrides(inject.bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, UkAndForeignPropertyCheckYourAnswersRoute)
        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]

        val result = controller.onSubmit(taxYear)(request)

        val thrown = intercept[NotFoundException] {
          await(result)
        }
        thrown.getMessage mustEqual "Uk and foreign property about section is not present in userAnswers"
      }
    }
  }
}
