package controllers

import controllers.actions._
import forms.ClaimExpensesOrRRRFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.ClaimExpensesOrRRRPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ClaimExpensesOrRRRView

import scala.concurrent.{ExecutionContext, Future}

class ClaimExpensesOrRRRController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ClaimExpensesOrRRRFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ClaimExpensesOrRRRView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {



  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      val preparedForm = request.userAnswers.get(ClaimExpensesOrRRRPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, taxYear, request.user.isAgentMessageKey, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val form = formProvider(request.user.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, taxYear, request.user.isAgentMessageKey, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ClaimExpensesOrRRRPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ClaimExpensesOrRRRPage, taxYear, mode, request.userAnswers, updatedAnswers))
      )
  }
}
