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

import audit.{AuditModel, AuditService}
import com.google.inject.Inject
import controllers.PropertyDetailsHandler
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreign.routes.ForeignTaxSectionCompleteController
import models._
import models.requests.DataRequest
import pages.foreign.Country
import pages.foreign.income.{ForeignPropertyTax, ReadForeignPropertyTax}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.{ClaimForeignTaxCreditReliefSummary, ForeignIncomeTaxSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignTaxCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ForeignTaxCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  businessService: BusinessService,
  view: ForeignTaxCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val list = SummaryListViewModel(
        rows = Seq(
          ForeignIncomeTaxSummary.row(taxYear, request.user.isAgentMessageKey, countryCode, request.userAnswers),
          ClaimForeignTaxCreditReliefSummary
            .row(taxYear, request.user.isAgentMessageKey, countryCode, request.userAnswers)
        ).flatten
      )

      Ok(view(list, taxYear, countryCode))
    }

  def onSubmit(taxYear: Int, countryCode: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ReadForeignPropertyTax(countryCode))
        .fold {
          val errorMsg =
            s"Foreign property tax section is missing for userId: ${request.userId}, taxYear: $taxYear, countryCode: $countryCode"
          logger.error(errorMsg)
          Future.successful(NotFound(errorMsg))
        } { foreignPropertyTax =>
          saveForeignTax(taxYear, request, foreignPropertyTax, countryCode)
        }
    }

  private def saveForeignTax(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyTax: ForeignPropertyTax,
    countryCode: String
  )(implicit hc: HeaderCarrier): Future[Result] =
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val context =
        JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyTax)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)

      propertySubmissionService
        .saveForeignPropertyJourneyAnswers(context, foreignPropertyTax)
        .map {
          case Right(_) =>
            Redirect(
              ForeignTaxSectionCompleteController.onPageLoad(taxYear, countryCode)
            )
          case Left(error) =>
            logger.error(s"Failed to save Foreign Tax section: ${error.toString}")
            throw SaveJourneyAnswersFailed("Failed to save Foreign Tax section")
        }
        .andThen {
          case Success(_) =>
            auditCYA(taxYear, request, foreignPropertyTax, isFailed = false, accrualsOrCash)
          case Failure(_) =>
            auditCYA(taxYear, request, foreignPropertyTax, isFailed = true, accrualsOrCash)
        }
    }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyTax: ForeignPropertyTax,
    isFailed: Boolean,
    accrualsOrCash: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.affinityGroup,
      request.user.nino,
      request.user.mtditid,
      taxYear,
      propertyType = AuditPropertyType.ForeignProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.ForeignProperty,
      sectionName = SectionName.ForeignPropertyTax,
      accountingMethod = if (accrualsOrCash) AccountingMethod.Traditional else AccountingMethod.Cash,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      foreignPropertyTax: ForeignPropertyTax
    )

    audit.sendAuditEvent(auditModel)
  }
}
