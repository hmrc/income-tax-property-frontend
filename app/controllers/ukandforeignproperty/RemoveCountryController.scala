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
import forms.ukandforeignproperty.RemoveCountryFormProvider
import handlers.ErrorHandler
import models.requests.DataRequest
import models.{Index, Mode}
import pages.ukandforeignproperty.SelectCountryPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.UkAndForeignPropertyCountryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ukandforeignproperty.RemoveCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCountryController @Inject() (
  override val messagesApi: MessagesApi,
  removeCountryService: UkAndForeignPropertyCountryService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCountryView,
  standardErrorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(taxYear: Int, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      renderPage(taxYear, index, mode, form, Ok)
    }

  def onSubmit(taxYear: Int, index: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => renderPage(taxYear, index, mode, formWithErrors, BadRequest),
          value =>
            if (value) {
              removeCountryService
                .removeCountry(index)
                .map { _ =>
                  Redirect(routes.ForeignCountriesRentedController.onPageLoad(taxYear, mode))
                }
                .recoverWith { case _: IndexOutOfBoundsException =>
                  standardErrorHandler.notFound()
                }
            } else {
              Future.successful(Redirect(routes.ForeignCountriesRentedController.onPageLoad(taxYear, mode)))
            }
        )

    }

  private def renderPage(taxYear: Int, index: Index, mode: Mode, formWithErrors: Form[Boolean], status: Status)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] =
    request.userAnswers
      .get(SelectCountryPage)
      .flatMap(countries => countries.lift(index.positionZeroIndexed)) match {
      case Some(country) =>
        Future.successful(status(view(formWithErrors, taxYear, mode, index, country)))
      case _ =>
        standardErrorHandler.notFound()
    }
}
