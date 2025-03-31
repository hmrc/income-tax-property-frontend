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

package controllers.foreign

import controllers.actions._
import forms.ForeignChangePIAExpensesFormProvider

import javax.inject.Inject
import models.Mode
import navigation.ForeignPropertyNavigator
import pages.foreign.adjustments.ForeignAdjustmentsCompletePage
import pages.foreign.allowances.ForeignAllowancesCompletePage
import pages.foreign.expenses.ForeignExpensesSectionCompletePage
import pages.foreign.income.ForeignIncomeSectionCompletePage
import pages.foreign.structurebuildingallowance.ForeignSbaCompletePage
import pages.foreign.{IncomeSourceCountries, ForeignPropertySummaryPage, ForeignTaxSectionCompletePage, ForeignChangePIAExpensesPage, ClaimPropertyIncomeAllowanceOrExpensesPage}
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreign.ForeignChangePIAExpensesView

import scala.concurrent.{ExecutionContext, Future}

class ForeignChangePIAExpensesController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: ForeignPropertyNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ForeignChangePIAExpensesFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ForeignChangePIAExpensesView,
                                         languageUtils: LanguageUtils
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      Ok(view(taxYear, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val countryList = request.userAnswers.get(IncomeSourceCountries).map(_.array.toList.flatMap { country =>
          CountryNamesDataSource.getCountry(country.code, languageUtils.getCurrentLang.locale.toString)
        })
        .toList
        .flatten
        .map(c => c.code)
      println(s"\n\n\nCountries: $countryList\n\n\n")
//        def clearData = test.foreach(country => {
//          request.userAnswers.remove(ForeignAdjustmentsCompletePage(country))
//          request.userAnswers.remove(ForeignAllowancesCompletePage(country))
//          request.userAnswers.remove(ForeignExpensesSectionCompletePage(country))
//          request.userAnswers.remove(ForeignIncomeSectionCompletePage(country))
//          request.userAnswers.remove(ForeignTaxSectionCompletePage(country))
//          request.userAnswers.remove(ForeignSbaCompletePage(country))
//        })

        for {

            updatedAnswers <- Future.fromTry(countryList.foreach(country => ForeignChangePIAExpensesPage.clean(Some(false), request.userAnswers, country)))

             // Future.fromTry(request.userAnswers.remove(ForeignAdjustmentsCompletePage("")))
            _              <- sessionRepository.set(updatedAnswers)
          } yield
    Redirect(navigator.nextPage(ForeignChangePIAExpensesPage, taxYear, mode, request.userAnswers, updatedAnswers))
  }
}
