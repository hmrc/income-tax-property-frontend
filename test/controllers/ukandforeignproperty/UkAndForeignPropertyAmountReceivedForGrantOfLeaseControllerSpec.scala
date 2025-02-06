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
import controllers.ukandforeignproperty.UkAndForeignPropertyHelper._
import forms.ukandforeignproperty.UkAndForeignPropertyAmountReceivedForGrantOfLeaseFormProvider
import models.NormalMode
import models.ukAndForeign.UkAndForeignPropertyAmountReceivedForGrantOfLease
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.UkAmountReceivedForGrantOfLeasePage
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.UkAndForeignPropertyAmountReceivedForGrantOfLeaseView

import scala.concurrent.Future

class UkAndForeignPropertyAmountReceivedForGrantOfLeaseControllerSpec extends SpecBase with MockitoSugar {

  val testTaxYear = 2024
  val formProvider = new UkAndForeignPropertyAmountReceivedForGrantOfLeaseFormProvider()
  def onwardRoute: Call = Call("GET", "/foo")

  "GET" - {

    val answers = Seq(
      emptyUserAnswers,
      emptyUserAnswers
        .set(
          UkAmountReceivedForGrantOfLeasePage,
          UkAndForeignPropertyAmountReceivedForGrantOfLease(34.56)
        ).get
    )

    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answers
    } yield (userType, agent, answer)

    userTypeAndAnswerCombinations.foreach { case (userType, agent, answer) =>
      s"Return OK for user type $userType and userAnswer is ${answer.get(UkAmountReceivedForGrantOfLeasePage)}" in {
        val application: Application = applicationBuilder(Some(answer), agent).build()
        running(application) {
          val controller =
            application.injector.instanceOf[UkAndForeignPropertyAmountReceivedForGrantOfLeaseController]
          val view = application.injector.instanceOf[UkAndForeignPropertyAmountReceivedForGrantOfLeaseView]
          val form = answer
            .get(UkAmountReceivedForGrantOfLeasePage)
            .fold(formProvider(userType))(formProvider(userType).fill)
          val result = controller.onPageLoad(testTaxYear, NormalMode)(FakeRequest())
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, testTaxYear, NormalMode, userType)(
            FakeRequest(),
            messages(application)
          ).toString
        }
      }
    }
  }

  "POST" - {
    val answersAndExpectedStatus = Seq(
      (Seq(), BAD_REQUEST),
      (Seq(("amountReceivedForGrantOfLease", "1.o1")), BAD_REQUEST),
      (Seq(("amountReceivedForGrantOfLease", "abcde")), BAD_REQUEST),
      (Seq(("amountReceivedForGrantOfLease", "999.999")), BAD_REQUEST),
      (Seq(("amountReceivedForGrantOfLease", "999.99")), SEE_OTHER),
    )

    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answersAndExpectedStatus
    } yield (userType, agent, answer)

    userTypeAndAnswerCombinations.foreach { case (userType, agent, (payload, expectedStatus)) =>
      s"Return $expectedStatus for user type $userType grant tax is $payload " in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Option(emptyUserAnswers), agent)
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val controller =
            application.injector.instanceOf[UkAndForeignPropertyAmountReceivedForGrantOfLeaseController]
          val result = controller.onSubmit(testTaxYear, NormalMode)(
            FakeRequest().withFormUrlEncodedBody(payload: _*)
          )
          status(result) mustBe expectedStatus
        }
      }
    }
  }
}
