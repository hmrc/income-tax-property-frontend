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

package controllers.propertyrentals

import audit.{AuditModel, AuditService, RentalsAbout}
import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.SaveJourneyAnswersFailed
import models.JourneyPath.PropertyRentalAbout
import models._
import models.requests.DataRequest
import pages.foreign.Country
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.propertyrentals.ClaimPropertyIncomeAllowanceSummary
import viewmodels.govuk.summarylist._
import views.html.propertyrentals.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class PropertyRentalsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView,
  audit: AuditService,
  propertySubmissionService: PropertySubmissionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ClaimPropertyIncomeAllowanceSummary
            .rows(taxYear, request.userAnswers, request.user.isAgentMessageKey, propertyType)
        ).flatten
      )

      Ok(view(list, taxYear))
    }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers
        .get(RentalsAbout)
        .map(propertyRentalsAbout => savePropertyAbout(taxYear, request, propertyRentalsAbout))
        .getOrElse {
          logger.error("Rentals About Section is not present in userAnswers")
          Future.failed(NotFoundException)
        }
  }

  private def savePropertyAbout(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyRentalsAbout: RentalsAbout
  )(implicit
    hc: HeaderCarrier
  ): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, PropertyRentalAbout)
    propertySubmissionService.saveJourneyAnswers(context, propertyRentalsAbout).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, propertyRentalsAbout, isFailed = false, AccountingMethod.Traditional)
        Future.successful(
          Redirect(controllers.propertyrentals.routes.AboutPropertyRentalsSectionFinishedController.onPageLoad(taxYear))
        )
      case Left(error) =>
        logger.error(s"Failed to save Rentals About section: ${error.toString}")
        auditCYA(taxYear, request, propertyRentalsAbout, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Rentals About section"))
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    propertyRentalsAbout: RentalsAbout,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.UKProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.Rentals,
      sectionName = SectionName.About,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = propertyRentalsAbout
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}

case object NotFoundException extends Exception("Rentals About Section is not present in userAnswers")
