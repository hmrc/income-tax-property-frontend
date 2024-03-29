/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.session.PropertyPeriodSessionRecovery
import models.backend.PropertyDetails
import models.requests.OptionalDataRequest
import pages._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.{BusinessService, PropertyPeriodSubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SummaryController @Inject()(
                                   val controllerComponents: MessagesControllerComponents,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   sessionRecovery: PropertyPeriodSessionRecovery,
                                   view: SummaryView,
                                   propertyPeriodSubmissionService: PropertyPeriodSubmissionService,
                                   sessionRepository: SessionRepository,
                                   businessService: BusinessService
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      withUpdatedData(taxYear) {
        businessService.getBusinessDetails(request.user).map {
          case Right(businessDetails) if businessDetails.propertyData.exists(existsUkProperty) =>
            val propertyData = businessDetails.propertyData.find(existsUkProperty).get
            val propertyRentalsRows = SummaryPage.createUkPropertyRows(request.userAnswers, taxYear, propertyData.cashOrAccruals.get)
            val fhlRows = SummaryPage.createFHLRows(request.userAnswers, taxYear, propertyData.cashOrAccruals.get)
            Ok(view(taxYear, propertyRentalsRows, fhlRows))
          case _ => Redirect(routes.SummaryController.show(taxYear))
        }
      }(request, controllerComponents.executionContext, hc)
  }

  private def existsUkProperty(property: PropertyDetails): Boolean =
    property.incomeSourceType.contains("uk-property") && property.tradingStartDate.isDefined && property.cashOrAccruals.isDefined

  private def withUpdatedData(taxYear: Int)(block: => Future[Result])
                             (implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    sessionRecovery.withUpdatedData(taxYear)(block)
  }
}
