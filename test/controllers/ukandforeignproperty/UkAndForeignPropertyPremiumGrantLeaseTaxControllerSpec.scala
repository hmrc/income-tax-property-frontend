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
import controllers.ukandforeignproperty.UkAndForeignPropertyHelper._
import forms.ukandforeignproperty.UkAndForeignPropertyPremiumGrantLeaseTaxFormProvider
import models.ukAndForeign.UkAndForeignPropertyPremiumGrantLeaseTax
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.UkPremiumGrantLeaseTaxPage
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.ukandforeignproperty.UkAndForeignPropertyPremiumGrantLeaseTaxView

import scala.concurrent.Future

class UkAndForeignPropertyPremiumGrantLeaseTaxControllerSpec extends SpecBase with MockitoSugar {

  val testTaxYear = 2024
  val formProvider = new UkAndForeignPropertyPremiumGrantLeaseTaxFormProvider()
  def onwardRoute: Call = Call("GET", "/foo")

  "GET" - {

    val answers = Seq(
      (Some(emptyUserAnswers), ""),
      (
        emptyUserAnswers
          .set(
            UkPremiumGrantLeaseTaxPage,
            UkAndForeignPropertyPremiumGrantLeaseTax(isPremiumGrantLease = true,Some(34.56))
          )
          .toOption,
        "true"
      ),
      (
        emptyUserAnswers
          .set(
            UkPremiumGrantLeaseTaxPage,
            UkAndForeignPropertyPremiumGrantLeaseTax(isPremiumGrantLease = false,None)
          )
          .toOption,
        "false"
      )
    )

    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answers
    } yield (userType, agent, answer)

    userTypeAndAnswerCombinations.foreach { case (userType, agent, (Some(userAnswer), value)) =>
      s"Return OK for user type $userType and userAnswer is ${userAnswer.get(UkPremiumGrantLeaseTaxPage)}" in {
        val application: Application = applicationBuilder(Some(userAnswer), agent).build()
        running(application) {
          val controller =
            application.injector.instanceOf[UkAndForeignPropertyPremiumGrantLeaseTaxController]
          val view = application.injector.instanceOf[UkAndForeignPropertyPremiumGrantLeaseTaxView]
          val form = userAnswer
            .get(UkPremiumGrantLeaseTaxPage)
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
      (Seq(("isPremiumGrantLease","true")), BAD_REQUEST),
      (Seq(("isPremiumGrantLease","true"), ("premiumGrantLeaseAmount", "1.o1")), BAD_REQUEST),
      (Seq(("isPremiumGrantLease","true"), ("premiumGrantLeaseAmount", "abcde")), BAD_REQUEST),
      (Seq(("isPremiumGrantLease","true"), ("premiumGrantLeaseAmount", "999.999")), BAD_REQUEST),
      (Seq(("isPremiumGrantLease","true"), ("premiumGrantLeaseAmount", "999.99")), SEE_OTHER),
      (Seq(("isPremiumGrantLease","false")), SEE_OTHER)
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
            application.injector.instanceOf[UkAndForeignPropertyPremiumGrantLeaseTaxController]
          val result = controller.onSubmit(testTaxYear, NormalMode)(
            FakeRequest().withFormUrlEncodedBody(payload: _*)
          )
          status(result) mustBe expectedStatus
        }
      }
    }
  }
}
