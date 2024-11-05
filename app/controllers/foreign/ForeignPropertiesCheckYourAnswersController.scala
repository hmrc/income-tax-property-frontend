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

package controllers.foreign

import controllers.actions._
import controllers.exceptions.InternalErrorFailure
import models.ForeignPropertyAbout
import models.requests.DataRequest
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.CountriesRentedPropertySummary
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignPropertiesCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.Future

class ForeignPropertiesCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: ForeignPropertiesCheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          CountriesRentedPropertySummary.row(taxYear, request.userAnswers)
        ).flatten
      )
      Ok(view(list, taxYear))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(ForeignPropertyAbout) match {
        case Some(foreignPropertiesAbout) =>
          saveForeignPropertiesInformation(taxYear, request, foreignPropertiesAbout)
        case None =>
          logger.error("Foreign Properties Section is not present in userAnswers")
          Future.failed(InternalErrorFailure("Foreign Properties Section is not present in userAnswers"))
      }
  }

  private def saveForeignPropertiesInformation(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyAbout: ForeignPropertyAbout
  )(implicit hc: HeaderCarrier): Future[Result] =
    Future.successful(
      Redirect(controllers.foreign.routes.ForeignPropertiesCheckYourAnswersController.onPageLoad(taxYear))
    )
}
