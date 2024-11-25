package controllers.foreign.expenses

import controllers.actions._
import forms.foreign.expenses.ForeignPropertyRepairsAndMaintenanceFormProvider
import models.Mode
import navigation.Navigator
import pages.foreign.expenses.ForeignPropertyRepairsAndMaintenancePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForeignPropertyRepairsAndMaintenanceController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ForeignPropertyRepairsAndMaintenanceFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ForeignPropertyRepairsAndMaintenanceView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ForeignPropertyRepairsAndMaintenancePage) match {
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
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ForeignPropertyRepairsAndMaintenancePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ForeignPropertyRepairsAndMaintenancePage, mode, updatedAnswers))
      )
  }
}
