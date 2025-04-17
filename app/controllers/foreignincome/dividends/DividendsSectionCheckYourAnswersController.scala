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

package controllers.foreignincome.dividends

import controllers.actions._

import javax.inject.Inject
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends.{IncomeBeforeForeignTaxDeductedSummary, CountryReceiveDividendIncomeSummary, ClaimForeignTaxCreditReliefSummary}
import views.html.DividendsSectionCheckYourAnswersView

class DividendsSectionCheckYourAnswersController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DividendsSectionCheckYourAnswersView,
                                       languageUtils: LanguageUtils
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val summaryListRows = Seq(
        CountryReceiveDividendIncomeSummary.row(taxYear, index, request.userAnswers, languageUtils.getCurrentLang.locale.toString),
        IncomeBeforeForeignTaxDeductedSummary.row(taxYear, countryCode, request.userAnswers),
        //Was foreign tax deducted?,
        //How much foreign tax was deducted?,
        ClaimForeignTaxCreditReliefSummary.row(taxYear, countryCode, request.user.isAgentMessageKey, request.userAnswers)
      ).flatten

      val list = SummaryListViewModel(
        rows = summaryListRows
      )
      Ok(view(list, taxYear))
  }

  //got to here

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(RentalsAdjustment) match {
        case Some(adjustments) => saveAdjustments(taxYear, request, adjustments)
        case None =>
          logger.error("Adjustments Section is not present in userAnswers")
          Future.failed(InternalErrorFailure("Adjustments Section is not present in userAnswers"))
      }
  }

  private def saveAdjustments(taxYear: Int, request: DataRequest[AnyContent], adjustments: RentalsAdjustment)(implicit
                                                                                                              hc: HeaderCarrier
  ) = {
    val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, PropertyRentalAdjustments)
    propertySubmissionService.saveUkPropertyJourneyAnswers(context, adjustments).flatMap {
      case Right(_) =>
        auditCYA(taxYear, request, adjustments, isFailed = false, AccountingMethod.Traditional)
        Future
          .successful(Redirect(controllers.adjustments.routes.RentalsAdjustmentsCompleteController.onPageLoad(taxYear)))
      case Left(error) =>
        logger.error(s"Failed to save Rentals Adjustments section: ${error.toString}")
        auditCYA(taxYear, request, adjustments, isFailed = true, AccountingMethod.Traditional)
        Future.failed(SaveJourneyAnswersFailed("Failed to save Rentals Adjustments section"))
    }
  }

  private def auditCYA(
                        taxYear: Int,
                        request: DataRequest[AnyContent],
                        adjustments: RentalsAdjustment,
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
      sectionName = SectionName.Adjustments,
      accountingMethod = accountingMethod,
      isUpdate = false,
      isFailed = isFailed,
      request.user.agentRef,
      adjustments
    )

    audit.sendRentalsAuditEvent(auditModel)
  }
}
