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

package controllers.foreign.structuresbuildingallowance

import controllers.actions._
import forms.ForeignSbaClaimsFormProvider
import models.UserAnswers
import play.api.Logging
import play.api.data.Form
import navigation.ForeignPropertyNavigator
import pages.ForeignSbaClaimsPage
import pages.foreign.structurebuildingallowance.ForeignStructureBuildingAllowanceGroup
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.govuk.all.SummaryListViewModel
import views.html.foreign.structurebuildingallowance.ForeignSbaClaimsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignSbaClaimsController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: ForeignPropertyNavigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ForeignSbaClaimsFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ForeignSbaClaimsView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, countryCode: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val form: Form[Boolean] = formProvider()
      val list: SummaryList = summaryList(taxYear, countryCode, request.userAnswers)

      Ok(view(form, list, taxYear, countryCode, request.user.isAgentMessageKey))
  }

//  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
//    implicit request =>
//
//      form.bindFromRequest().fold(
//        formWithErrors =>
//          Future.successful(BadRequest(view(formWithErrors, mode))),
//
//        value =>
//          for {
//            updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignSbaClaimsPage, value))
//            _              <- sessionRepository.set(updatedAnswers)
//          } yield Redirect(navigator.nextPage(ForeignSbaClaimsPage, mode, updatedAnswers))
//      )
//  }

  private def summaryList(taxYear: Int, countryCode: String, userAnswers: UserAnswers)(implicit
                                                                                              messages: Messages
  ) = {
    val sbaEntries = userAnswers.get(ForeignStructureBuildingAllowanceGroup(countryCode)).toSeq.flatten
    val rows = sbaEntries.zipWithIndex.flatMap { case (_, index) =>
      StructureBuildingAllowanceSummary.row(taxYear, index, userAnswers, propertyType)
    }
    SummaryListViewModel(rows)
  }
}
