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
import models.ukAndForeign.{AboutForeign, AboutUk, AboutUkAndForeign, UkAndForeignAbout}
import models.{JourneyContext, JourneyPath, TotalPropertyIncome, UserAnswers}
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

  val taxYear: Int = LocalDate.now.getYear
  lazy val UkAndForeignPropertyCheckYourAnswersRoute: String =
    controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear = taxYear).url

  val aboutUkAndForeign: AboutUkAndForeign = AboutUkAndForeign(
    totalPropertyIncome = TotalPropertyIncome.LessThan,
    reportIncome = Some(DoNoWantToReport),
    ukPropertyRentalType = None,
    countries = None,
    claimExpensesOrRelief = None,
    claimPropertyIncomeAllowanceOrExpenses = None
  )

  val aboutUk: AboutUk = AboutUk(
    nonUkResidentLandlord = None,
    deductingTaxFromNonUkResidentLandlord = None,
    ukRentalPropertyIncomeAmount = None,
    balancingCharge = None,
    premiumForLease = None,
    premiumGrantLeaseTax = None,
    amountReceivedForGrantOfLeasePage = None,
    yearLeaseAmount = None,
    premiumsGrantLease = None,
    reversePremiumsReceived = None,
    otherIncomeFromProperty = None
  )
  val aboutForeign: AboutForeign = AboutForeign(
    foreignRentalPropertyIncomeAmount = None,
    foreignBalancingCharge = None,
    foreignPremiumsForTheGrantOfALease = None,
    foreignCalculatedPremiumGrantLeaseTaxable = None,
    foreignLeaseGrantReceivedAmount = None,
    foreignYearLeaseAmount = None,
    foreignPremiumsGrantLease = None,
    foreignOtherIncomeFromProperty = None,
    propertyIncomeAllowanceClaim = None
  )
  val ukAndForeignAbout: UkAndForeignAbout = UkAndForeignAbout(aboutUkAndForeign, Some(aboutUk), Some(aboutForeign))

  "UkAndForeignPropertyCheckYourAnswers Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()

      running(application) {
        val request = FakeRequest(GET, UkAndForeignPropertyCheckYourAnswersRoute)

        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]
        val view = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)
        val ukList = None
        val foreignList = None

        val result = controller.onPageLoad(taxYear)(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, ukList, foreignList, taxYear)(request, messages(application)).toString
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

      val context = {
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = JourneyPath.UkAndForeignPropertyAbout)
      }

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
          FakeRequest(POST, controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onSubmit(taxYear = taxYear).url)
        val controller = application.injector.instanceOf[UkAndForeignPropertyCheckYourAnswersController]

        val result = controller.onSubmit(taxYear)(request)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.ukandforeignproperty.routes.SectionCompleteController.onPageLoad(taxYear: Int).url
      }
    }

    "must return NotFoundException when UkAndForeignAbout section is missing in userAnswers" in {

      val userAnswers = UserAnswers("test")

      val context =
        JourneyContext(taxYear = taxYear, mtditid = "mtditid", nino = "nino", journeyPath = JourneyPath.UkAndForeignPropertyAbout)

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
          FakeRequest(POST, controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onSubmit(taxYear = taxYear).url)
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
