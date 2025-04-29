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

package controllers.foreignincome.dividends

import audit.AuditService
import base.SpecBase
import controllers.foreignincome.dividends.routes.{DividendsSectionCheckYourAnswersController, DividendsSectionFinishedController}
import models.JourneyPath.ForeignDividends
import models.{UserAnswers, TotalIncome, JourneyContext}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.Country
import pages.foreignincome.dividends.DividendsSectionFinishedPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{BusinessService, PropertySubmissionService}
import viewmodels.govuk.SummaryListFluency
import views.html.foreignincome.dividends.DividendsSectionCheckYourAnswersView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DividendsSectionCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency {
  val taxYear = 2024


  "DividendsSectionCheckYourAnswers Controller" - {

    def submitOnwardRoute: Call = Call(
      "POST",
      "/update-and-submit-income-tax-return/property/2024/foreign-income/dividends/section-finished"
    )

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = false).build()
      val list = SummaryListViewModel(Seq.empty)

      running(application) {
        val request = FakeRequest(GET, DividendsSectionCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DividendsSectionCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, taxYear)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None, isAgent = false).build()

      running(application) {
        val request = FakeRequest(GET, DividendsSectionCheckYourAnswersController.onPageLoad(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and the POST for onSubmit() should redirect to the Have you finished this section page" in {
      val userAnswers = UserAnswers("test").set(DividendsSectionFinishedPage, false).get
      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = ForeignDividends)

      val userAnswersForeignDividends =
        userAnswers
          .set(
            models.ForeignDividends,
            models.ForeignDividends(Array(Country("Australia", "AUS")), foreignTaxDeductedFromDividendIncome = true)
          )
          .get

//      when(
//        propertySubmissionService
//          .saveForeignPropertyJourneyAnswers(ArgumentMatchers.eq(context), any)(
//            any(),
//            any()
//          )
//      ) thenReturn Future(Right())

      when(businessService.getForeignPropertyDetails(any(), any())(any())) thenReturn Future(
        Right(Some(foreignPropertyDetails))
      )

      val application = applicationBuilder(userAnswers = Some(userAnswersForeignDividends), isAgent = true)
//        .overrides(bind[PropertySubmissionService].toInstance(propertySubmissionService))
        .overrides(bind[BusinessService].toInstance(businessService))
        .overrides(bind[AuditService].toInstance(audit))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, DividendsSectionCheckYourAnswersController.onSubmit(taxYear).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
//        verify(audit, times(1)).sendAuditEvent(any())(any(), any())
        redirectLocation(result).value mustEqual submitOnwardRoute.url
      }
    }
  }
}
