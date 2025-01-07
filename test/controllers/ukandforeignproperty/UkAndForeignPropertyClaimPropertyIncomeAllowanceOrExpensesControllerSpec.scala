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
import forms.ukandforeignproperty.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesFormProvider
import models.{NormalMode, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses, UkAndForeignPropertyRentalTypeUk}
import org.scalatestplus.mockito.MockitoSugar
import pages.UkAndForeignPropertyRentalTypeUkPage
import pages.ukandforeignproperty.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage
import play.api.Application
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ukandforeignproperty.UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesView

class UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesControllerSpec extends SpecBase with MockitoSugar {

  val testTaxYear  = 2024
  val formProvider = new UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesFormProvider()

  "GET" - {

    val answers = Seq(
      (Some(emptyUserAnswers), ""),
      (emptyUserAnswers.set(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(true) ).toOption,"true"),
      (emptyUserAnswers.set(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage, UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses(false) ).toOption, "false")
    )

    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answers
    } yield (userType, agent, answer)

    userTypeAndAnswerCombinations.foreach{
      case (userType, agent, (Some(userAnswer), value)) =>
        s"Return OK for user type $userType and allowance is $value" in {
          val application: Application = applicationBuilder(Some(userAnswer), agent).build()
          running(application){
            val controller = application.injector.instanceOf[UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController]
            val view       = application.injector.instanceOf[UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesView]
            val form       = userAnswer.get(UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesPage).fold(formProvider())(formProvider().fill)
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

    val propertyRentalTypeUk = Seq(
      Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.PropertyRentals),
      Set[UkAndForeignPropertyRentalTypeUk](UkAndForeignPropertyRentalTypeUk.RentARoom),
      UkAndForeignPropertyRentalTypeUk.values.toSet[UkAndForeignPropertyRentalTypeUk],
    )


    val userTypeAndAnswerCombinations = for {
      (userType, agent) <- userTypes
      answer            <- answers
      rentalTypeUk      <- propertyRentalTypeUk
    } yield (userType, agent, answer, rentalTypeUk)

    userTypeAndAnswerCombinations.foreach{
      case (userType, agent, (value, expectedStatus), rentalType) =>
        s"Return $expectedStatus for user type $userType and allowance is $value and rental type is $rentalType" in {
          val userAnswers = emptyUserAnswers.set(UkAndForeignPropertyRentalTypeUkPage, rentalType).toOption
          val application: Application = applicationBuilder(userAnswers, agent).build()
          running(application){
            val controller = application.injector.instanceOf[UkAndForeignPropertyClaimPropertyIncomeAllowanceOrExpensesController]
            val result     = controller.onSubmit(testTaxYear, NormalMode)(
              FakeRequest().withFormUrlEncodedBody(
                ("ukAndForeignPropertyClaimPropertyIncomeAllowanceOrExpenses", value)
              )
            )
            expectedStatus match {
              case SEE_OTHER =>
                assertThrows[Throwable](status(result))
              case _ =>
                status(result) mustBe expectedStatus
            }
          }
        }
    }
  }
}
