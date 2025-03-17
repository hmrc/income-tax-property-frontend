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

package controllers.foreign.allowances

import controllers.actions._
import controllers.{PropertyDetailsHandler, routes}
import models.backend.PropertyDetails
import navigation.ForeignPropertyNavigator
import pages.foreign.IncomeSourceCountries
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, CountryNamesDataSource}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.language.LanguageUtils
import views.html.foreign.allowances.ForeignPropertyAllowancesStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyAllowancesStartController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  foreignNavigator: ForeignPropertyNavigator,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignPropertyAllowancesStartView,
  businessService: BusinessService,
  languageUtils: LanguageUtils
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val maybeCountryName =
        request.userAnswers
          .get(IncomeSourceCountries)
          .map(_.array.toList.flatMap { country =>
            CountryNamesDataSource.getCountry(country.code, languageUtils.getCurrentLang.locale.toString)
          })
          .flatMap(country => country.find(_.code == countryCode))
          .map(_.name)
      val countryName = maybeCountryName.getOrElse("")

      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      withForeignPropertyDetails[Result](businessService, request.user.nino, request.user.mtditid) {
        (propertyData: PropertyDetails) =>
          propertyData.accrualsOrCash match {
            case Some(true) =>
              logger.info("Accounting method: Accruals")
              Future.successful(
                Ok(view(taxYear, countryCode, countryName, request.user.isAgentMessageKey, accrualsOrCash = true))
              )
            case Some(false) =>
              logger.info("Accounting method: Cash")
              Future.successful(
                Ok(view(taxYear, countryCode, countryName, request.user.isAgentMessageKey, accrualsOrCash = false))
              )
            case _ =>
              Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
      }(hc, ec)
    }

}
