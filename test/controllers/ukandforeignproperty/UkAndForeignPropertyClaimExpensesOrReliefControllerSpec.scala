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
import forms.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefFormProvider
import models.{NormalMode, UkAndForeignPropertyClaimExpensesOrRelief, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefPage
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ukandforeignproperty.UkAndForeignPropertyClaimExpensesOrReliefView
import UkAndForeignPropertyHelper._

class UkAndForeignPropertyClaimExpensesOrReliefControllerSpec extends SpecBase with MockitoSugar {

  val testTaxYear  = 2024
  val formProvider = new UkAndForeignPropertyClaimExpensesOrReliefFormProvider()

  "GET" - {

    val answers = Seq(
      (Some(emptyUserAnswers),""),
      (emptyUserAnswers.set(UkAndForeignPropertyClaimExpensesOrReliefPage, UkAndForeignPropertyClaimExpensesOrRelief(true) ).toOption,"true"),
      (emptyUserAnswers.set(UkAndForeignPropertyClaimExpensesOrReliefPage, UkAndForeignPropertyClaimExpensesOrRelief(false) ).toOption,"false")
    )

    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answers
    } yield (userType, agent, answer)

    userTypeAndAnswerCombinations.foreach{
      case (userType, agent, (Some(userAnswer), isRelief)) =>
        s"Return OK for user type $userType and relief $isRelief" in {
          val application: Application = applicationBuilder(Some(userAnswer), agent).build()
          running(application){
            val controller = application.injector.instanceOf[UkAndForeignPropertyClaimExpensesOrReliefController]
            val view       = application.injector.instanceOf[UkAndForeignPropertyClaimExpensesOrReliefView]
            val form       = userAnswer.get(UkAndForeignPropertyClaimExpensesOrReliefPage).fold(formProvider(userType))(formProvider(userType).fill)
            val result     = controller.onPageLoad(testTaxYear, NormalMode)(FakeRequest())
            status(result) mustBe OK
            contentAsString(result) mustBe view(form, testTaxYear, NormalMode, userType)(FakeRequest(), messages(application)).toString
          }
        }
    }
  }

  "POST" - {
    val  answers = Seq(
      ("", BAD_REQUEST),
      ("true", SEE_OTHER),
      ("false", SEE_OTHER),
    )

    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answers
    } yield (userType, agent, answer)

    userTypeAndAnswerCombinations.foreach{
      case (userType, agent, (isRelief, expectedStatus)) =>
        s"Return $expectedStatus for user type $userType and Relief $isRelief" in {
          val application: Application = applicationBuilder(Some(emptyUserAnswers), agent).build()
          running(application){
            val controller = application.injector.instanceOf[UkAndForeignPropertyClaimExpensesOrReliefController]
            val result     = controller.onSubmit(testTaxYear, NormalMode)(
              FakeRequest().withFormUrlEncodedBody(
                ("ukAndForeignPropertyClaimExpensesOrRelief", isRelief)
              )
            )
            status(result) mustBe expectedStatus
          }
        }
    }
  }
}
