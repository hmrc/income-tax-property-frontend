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

package controllers.foreign.adjustments

import controllers.actions._
import models.{ForeignProperty, NormalMode}
import controllers.foreign.adjustments.routes.{ForeignPrivateUseAdjustmentController, ForeignUnusedLossesPreviousYearsController}
import pages.{getIsClaimPIA, isUkAndForeignAboutJourneyComplete}
import pages.foreign.IncomeSourceCountries
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.CountryNamesDataSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreign.adjustments.ForeignAdjustmentsStartView

import javax.inject.Inject

class ForeignAdjustmentsStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignAdjustmentsStartView,
  languageUtils: LanguageUtils
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String, isPIA: Boolean): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val maybeCountryName =
        request.userAnswers
          .get(IncomeSourceCountries)
          .map(_.array.toList.flatMap { country =>
            CountryNamesDataSource.getCountry(country.code, languageUtils.getCurrentLang.locale.toString)
          })
          .flatMap(country => country.find(_.code == countryCode))
          .map(_.name)
      val countryName = maybeCountryName.getOrElse("")
      val isUkAndForeignJourney: Boolean = isUkAndForeignAboutJourneyComplete(request.userAnswers)
      val continueLink: String =
        if(isUkAndForeignJourney && isPIA){
          ForeignUnusedLossesPreviousYearsController.onPageLoad(taxYear, countryCode, NormalMode).url
        } else {
        ForeignPrivateUseAdjustmentController.onPageLoad(taxYear, countryCode, NormalMode).url
      }
      Ok(view(taxYear, countryName, countryCode, isPIA, isUkAndForeignJourney, continueLink))

    }
}
