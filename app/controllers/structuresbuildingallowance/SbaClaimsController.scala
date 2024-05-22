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

package controllers.structuresbuildingallowance

import audit.{AuditModel, AuditService}
import controllers.actions._
import forms.structurebuildingallowance.SbaClaimsFormProvider
import models.requests.DataRequest
import models.{JourneyContext, NormalMode}
import navigation.Navigator
import pages.structurebuildingallowance._
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.structurebuildingallowance.StructureBuildingAllowanceClaimSummary
import viewmodels.govuk.summarylist._
import views.html.structurebuildingallowance.SbaClaimsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SbaClaimsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SbaClaimsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SbaClaimsView,
  propertySubmissionService: PropertySubmissionService,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      val list: SummaryList = summaryList(taxYear, request)

      Ok(view(form, list, taxYear, request.user.isAgentMessageKey))
  }

  def onSubmit(taxYear: Int): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      val list: SummaryList = summaryList(taxYear, request)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, list, taxYear, request.user.isAgentMessageKey))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SbaClaimsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
              _              <- if (!value) saveSBAClaims(taxYear, request) else Future.successful(())
            } yield Redirect(
              navigator.nextPage(SbaClaimsPage, taxYear, NormalMode, request.userAnswers, updatedAnswers)
            )
        )
  }

  private def summaryList(taxYear: Int, request: DataRequest[AnyContent])(implicit messages: Messages) = {
    val sbaForm = request.userAnswers.get(StructureBuildingFormGroup).getOrElse(Array())

    val rows = sbaForm.zipWithIndex
      .map(claim =>
        StructureBuildingAllowanceClaimSummary.row(taxYear, claim._2, claim._1.structureBuildingAllowanceClaim)
      )

    SummaryListViewModel(rows)
  }

  private def saveSBAClaims(taxYear: Int, request: DataRequest[AnyContent])(implicit hc: HeaderCarrier) =
    Future {
      val sbaInfoOpt = for {
        claimSummaryPage <- request.userAnswers.get(ClaimStructureBuildingAllowancePage)
        sbaGroup         <- request.userAnswers.get(StructureBuildingFormGroup)
      } yield SbaInfo(claimSummaryPage, sbaGroup)

      sbaInfoOpt.fold {
        logger.error("Structure and Building Allowance not found in userAnswers")
      } { sbaInfo =>
        val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, "property-rental-sba")
        propertySubmissionService.saveJourneyAnswers(context, sbaInfo).map {
          case Right(_) =>
            auditSBAClaims(taxYear, request, sbaInfo)
          case Left(_) => InternalServerError
        }
      }
    }

  private def auditSBAClaims(
    taxYear: Int,
    request: DataRequest[AnyContent],
    sbaInfo: SbaInfo
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel[SbaInfo](
      nino = request.user.nino,
      userType = request.user.affinityGroup,
      mtdItId = request.user.mtditid,
      agentRef = request.user.agentRef,
      taxYear = taxYear,
      isUpdate = false,
      sectionName = "PropertyRentalsSBA",
      enteredRentalDetails = sbaInfo
    )
    auditService.sendRentalsAuditEvent(auditModel)
  }

}
