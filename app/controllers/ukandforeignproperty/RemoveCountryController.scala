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

import controllers.actions._
import models.{Index, Mode}
import pages.ukandforeignproperty.SelectCountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import service.RemoveCountryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.RemoveCountryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RemoveCountryController @Inject() (override val messagesApi: MessagesApi,
                                         removeCountryService: RemoveCountryService,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemoveCountryView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(SelectCountryPage)
        .flatMap(countries => countries.toList.lift(index.positionZeroIndexed)) match {
        case Some(country) =>
          Ok(view(taxYear, mode, index, country))
        case _ =>
          NotFound("Country not found")
      }
    }

  def onSubmit(taxYear: Int, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      removeCountryService.removeCountry(index).map { _ =>
        Redirect(routes.ForeignCountriesRentedController.onPageLoad(taxYear, mode))
      }
    }

}
