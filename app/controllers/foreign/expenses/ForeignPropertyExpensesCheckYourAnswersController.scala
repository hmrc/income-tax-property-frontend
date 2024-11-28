package controllers.foreign.expenses

import controllers.actions._
import forms.ForeignPropertyExpensesCheckYourAnswersFormProvider
import views.html.foreign.expenses.ForeignPropertyExpensesCheckYourAnswersView
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.ForeignPropertyExpensesCheckYourAnswersPage
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyExpensesCheckYourAnswersController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: ForeignPropertyExpensesCheckYourAnswersFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: ForeignPropertyExpensesCheckYourAnswersView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ForeignPropertyExpensesCheckYourAnswersPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignPropertyExpensesCheckYourAnswersPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ForeignPropertyExpensesCheckYourAnswersPage, mode, updatedAnswers))
      )
  }
}
