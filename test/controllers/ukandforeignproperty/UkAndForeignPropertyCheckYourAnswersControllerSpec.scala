/*
 * Copyright 2024 HM Revenue & Customs
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
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, running, status}
//import service.PropertySubmissionService
import viewmodels.govuk.SummaryListFluency
import views.html.ukandforeignproperty.UkAndForeignPropertyCheckYourAnswersView

import java.time.LocalDate

class UkAndForeignPropertyCheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  //private val propertySubmissionService = mock[PropertySubmissionService]

  val countryCode: String = "USA"
  val taxYear: Int = LocalDate.now.getYear
  lazy val UkAndForeignPropertyCheckYourAnswersRoute: String =
    controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear = taxYear, mode = NormalMode).url
  def onwardRoute: Call = Call("GET", "/")
  val foreignTaxPaidOrDeducted: BigDecimal = 234

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
  }

}
