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

package controllers.enhancedstructuresbuildingallowance

import controllers.actions._
import forms.enhancedstructuresbuildingallowance.EsbaQualifyingDateFormProvider
import models.{Mode, PropertyType}
import navigation.Navigator
import pages.enhancedstructuresbuildingallowance.EsbaQualifyingDatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.enhancedstructuresbuildingallowance.EsbaQualifyingDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EsbaQualifyingDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: EsbaQualifyingDateFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: EsbaQualifyingDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(taxYear: Int, index: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val form: Form[LocalDate] = formProvider()
      val preparedForm = request.userAnswers.get(EsbaQualifyingDatePage(index, propertyType)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, index, mode, propertyType))
    }

  def onSubmit(taxYear: Int, index: Int, mode: Mode, propertyType: PropertyType): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val form: Form[LocalDate] = formProvider()
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, index, mode, propertyType))),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(request.userAnswers.set(EsbaQualifyingDatePage(index, propertyType), value))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator
                .esbaNextPage(
                  EsbaQualifyingDatePage(index, propertyType),
                  taxYear,
                  mode,
                  index,
                  request.userAnswers,
                  updatedAnswers
                )
            )
        )
    }
}
