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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.exceptions.{NotFoundException, SaveJourneyAnswersFailed}
import models.requests.DataRequest
import models._
import pages.foreign.income.ForeignPropertyTaxPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.foreign.{ClaimForeignTaxCreditReliefSummary, ForeignIncomeTaxSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.ForeignTaxCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ForeignTaxCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  view: ForeignTaxCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

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
        .get(ForeignPropertyTaxPage(countryCode))
        .map { foreignPropertyTax =>
          val foreignPropertyTaxWithCountryCode = ForeignPropertyTaxWithCountryCode(
            countryCode = countryCode,
            foreignIncomeTax = foreignPropertyTax.foreignIncomeTax,
            foreignTaxCreditRelief = foreignPropertyTax.foreignTaxCreditRelief
          )
          saveForeignTax(taxYear, request, foreignPropertyTaxWithCountryCode)
        }
        .getOrElse {
          logger.error(
            s"Foreign property tax is not present in userAnswers for userId: ${request.userId} "
          )
          Future.failed(
            NotFoundException("Foreign property tax is not present in userAnswers")
          )
        }
    }

  private def saveForeignTax(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyTaxWithCountryCode: ForeignPropertyTaxWithCountryCode
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignPropertyTax)
    propertySubmissionService.saveJourneyAnswers(context, foreignPropertyTaxWithCountryCode).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, foreignPropertyTaxWithCountryCode, isFailed = false, AccountingMethod.Traditional)
        Future.successful(
          Redirect(
            routes.ForeignTaxSectionCompleteController
              .onPageLoad(taxYear, foreignPropertyTaxWithCountryCode.countryCode)
          )
        )

      case Left(error) =>
        logger.error(
          s"Failed to save Foreign property tax ${foreignPropertyTaxWithCountryCode.countryCode} section: ${error.toString}"
        )
        auditCYA(taxYear, request, foreignPropertyTaxWithCountryCode, isFailed = true, AccountingMethod.Traditional)
        Future.failed(
          SaveJourneyAnswersFailed(
            s"Failed to save Foreign property tax ${foreignPropertyTaxWithCountryCode.countryCode} section"
          )
        )

    }

  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignPropertyTaxWithCountryCode: ForeignPropertyTaxWithCountryCode,
    isFailed: Boolean,
    accountingMethod: AccountingMethod
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      request.user.agentRef,
      taxYear,
      isUpdate = false,
      sectionName = SectionName.ForeignPropertyTax,
      propertyType = AuditPropertyType.ForeignProperty,
      journeyName = JourneyName.ForeignProperty,
      accountingMethod = accountingMethod,
      isFailed = isFailed,
      foreignPropertyTaxWithCountryCode
    )

    audit.sendAuditEvent(auditModel)
  }
}
