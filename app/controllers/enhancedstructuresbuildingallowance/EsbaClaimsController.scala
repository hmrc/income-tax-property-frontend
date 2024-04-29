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

package controllers.enhancedstructuresbuildingallowance

import audit.{AuditModel, AuditService}
import controllers.actions._
import forms.enhancedstructuresbuildingallowance.EsbaClaimsFormProvider
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.enhancedstructuresbuildingallowance.Esba._
import pages.enhancedstructuresbuildingallowance._
import play.api.data.Form
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.enhancedstructurebuildingallowance.EsbaClaimAmountSummary
import viewmodels.govuk.summarylist._
import views.html.enhancedstructuresbuildingallowance.EsbaClaimsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EsbaClaimsController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      sessionRepository: SessionRepository,
                                      navigator: Navigator,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: EsbaClaimsFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      audit: AuditService,
                                      view: EsbaClaimsView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


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

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, list, taxYear, request.user.isAgentMessageKey))),

        value => {
          request.userAnswers.get(Esbas) match {
            case Some(esbas) if value == false =>
              auditCYA(taxYear, request, esbas)
            case None =>
              logger.error("Enhanced Structured Building Allowance Section is not present in userAnswers")
            case _ => ()
          }

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(EsbaClaimsPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(EsbaClaimsPage, taxYear, NormalMode, request.userAnswers, updatedAnswers))
        }
      )
  }

  private def auditCYA(taxYear: Int, request: DataRequest[AnyContent], esbas: List[Esba])(implicit hc: HeaderCarrier): Unit = {
    val auditModel = AuditModel(
      request.user.nino,
      request.user.affinityGroup,
      request.user.mtditid,
      taxYear,
      isUpdate = false,
      "PropertyRentalsESBA",
      esbas
    )

    audit.sendRentalsAuditEvent(auditModel)
  }

  private def summaryList(taxYear: Int, request: DataRequest[AnyContent])(implicit messages: Messages) = {
    val esbaForm = request.userAnswers.get(EnhancedStructureBuildingFormGroup).getOrElse(Array())

    val rows = esbaForm.zipWithIndex
      .map(claim => EsbaClaimAmountSummary.row(taxYear, claim._2, claim._1.esbaClaim))

    SummaryListViewModel(rows)
  }

}

