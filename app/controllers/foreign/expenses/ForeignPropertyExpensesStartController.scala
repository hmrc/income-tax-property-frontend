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

package controllers.foreign.expenses

import controllers.actions._
import models.JourneyPath.PropertyAbout
import pages.foreign.income.ForeignPropertyRentalIncomePage
import pages.foreign.{Country, IncomeSourceCountries}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.foreign.expenses.ForeignPropertyExpensesStartView

import javax.inject.Inject

class ForeignPropertyExpensesStartController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ForeignPropertyExpensesStartView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear:Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val countryName = request.userAnswers.get(IncomeSourceCountries).map(_.array.toList).flatMap( countryList =>
        countryList.find{ country => country.code == countryCode
        }).get.name
      val income = request.userAnswers.get(ForeignPropertyRentalIncomePage(countryCode))
      income match {
        case Some(income) if income < 85000 =>
          Ok(view(taxYear, countryName, isIncomeUnder85k = true, request.user.isAgentMessageKey))
        case Some(income) if income >= 85000 =>
          Ok(view(taxYear, countryName, isIncomeUnder85k = false, request.user.isAgentMessageKey))
      }

  }
}
