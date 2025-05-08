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

import audit.{AuditModel, AuditService}
import controllers.PropertyDetailsHandler
import controllers.actions._
import controllers.exceptions.SaveJourneyAnswersFailed
import controllers.foreignincome.dividends.routes.{CountryReceiveDividendIncomeController, DividendsSectionFinishedController}
import forms.foreignincome.dividends.YourForeignDividendsByCountryFormProvider
import models.AccountingMethod.{Cash, Traditional}
import models.{AuditPropertyType, ForeignIncomeDividends, JourneyContext, JourneyName, JourneyPath, Mode, NormalMode, ReadForeignDividendsByCountry, SectionName, UserAnswers}
import models.requests.DataRequest
import navigation.ForeignIncomeNavigator
import pages.foreign.Country
import pages.foreignincome.{DividendIncomeSourceCountries, IncomeBeforeForeignTaxDeductedPage}
import pages.foreignincome.dividends.{ClaimForeignTaxCreditReliefPage, ForeignTaxDeductedFromDividendIncomePage, HowMuchForeignTaxDeductedFromDividendIncomePage, YourForeignDividendsByCountryPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import service.{BusinessService, PropertySubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.foreignincome.dividends.YourForeignDividendsByCountrySummary
import views.html.foreignincome.dividends.YourForeignDividendsByCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourForeignDividendsByCountryController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: YourForeignDividendsByCountryFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: YourForeignDividendsByCountryView,
  languageUtils: LanguageUtils,
  propertySubmissionService: PropertySubmissionService,
  audit: AuditService,
  businessService: BusinessService
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with PropertyDetailsHandler {

  def onPageLoad(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val currentLang = languageUtils.getCurrentLang.locale.toString
      val rows = YourForeignDividendsByCountrySummary.tableRows(taxYear, request.userAnswers, currentLang)
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      Ok(view(form, rows, taxYear, request.user.isAgentMessageKey, mode))
  }

  def onSubmit(taxYear: Int, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val currentLang = languageUtils.getCurrentLang.locale.toString
      val rows = YourForeignDividendsByCountrySummary.tableRows(taxYear, request.userAnswers, currentLang)
      val form: Form[Boolean] = formProvider(request.user.isAgentMessageKey)
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, rows, taxYear, request.user.isAgentMessageKey, mode))),
        addAnotherCountry => handleValidForm(addAnotherCountry, taxYear, request)
      )
  }

  private def handleValidForm(
    addAnotherCountry: Boolean,
    taxYear: Int,
    request: DataRequest[AnyContent]
  )(implicit hc: HeaderCarrier): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(YourForeignDividendsByCountryPage, addAnotherCountry))
      _ <- sessionRepository.set(updatedAnswers)
      result <- if(addAnotherCountry) {
        val countries = request.userAnswers.get(DividendIncomeSourceCountries).getOrElse(Array.empty)
        val index = getNextIndex(countries, Some(request.userAnswers))
        Future(Redirect(routes.CountryReceiveDividendIncomeController.onPageLoad(taxYear, index, NormalMode)))
      } else {
        saveJourneyAnswers(taxYear, request)
      }
    } yield result

  private def getNextIndex(countryArr: Array[Country], userAnswers: Option[UserAnswers]): Int =
    userAnswers.map { userAnswers =>
        countryArr.foldLeft(countryArr.length) { (acc, country) =>
          (
            userAnswers.get(IncomeBeforeForeignTaxDeductedPage(country.code)),
            userAnswers.get(ForeignTaxDeductedFromDividendIncomePage(country.code)),
            userAnswers.get(HowMuchForeignTaxDeductedFromDividendIncomePage(country.code)),
            userAnswers.get(ClaimForeignTaxCreditReliefPage(country.code))
          ) match {
            case (Some(_), Some(true), Some(_), Some(_)) => acc
            case (Some(_), Some(false), _, _)            => acc
            case _                                       => countryArr.indexOf(country) min acc
          }
        }
      }
      .getOrElse(0)

  private def saveJourneyAnswers(
    taxYear: Int,
    request: DataRequest[AnyContent]
  )(implicit hc: HeaderCarrier): Future[Result] = {
    request.userAnswers
      .get(DividendIncomeSourceCountries).map { countries =>
        countries.toSeq.flatMap(country => request.userAnswers
          .get(ReadForeignDividendsByCountry(country.code))
          .map(_.toForeignIncomeDividend(country.code))
        )
      }
      .filter(_.nonEmpty)
      .fold {
        val errorMsg = s"Foreign dividends section is missing for userId: ${request.userId}, taxYear: $taxYear"
        logger.error(errorMsg)
        Future.successful(NotFound(errorMsg))
      } { foreignDividends =>
        saveDividends(taxYear, request, ForeignIncomeDividends(foreignDividends))
      }
  }

  private def saveDividends(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignIncomeDividends: ForeignIncomeDividends
  )(implicit hc: HeaderCarrier): Future[Result] = {
    withForeignPropertyDetails(businessService, request.user.nino, request.user.mtditid) { propertyDetails =>
      val context = JourneyContext(taxYear, request.user.mtditid, request.user.nino, JourneyPath.ForeignIncomeDividends)
      val accrualsOrCash = propertyDetails.accrualsOrCash.getOrElse(true)
      propertySubmissionService.saveForeignDividendsJourneyAnswers(context, foreignIncomeDividends).flatMap {
        case Right(_) =>
          Future.successful(
            Redirect(
              routes.DividendsSectionFinishedController.onPageLoad(taxYear)
            )
          )
        case Left(error) =>
          logger.error(s"Failed to save Dividends section: ${error.toString}")
          Future.failed(SaveJourneyAnswersFailed("Failed to save Dividends section"))
      }.andThen { saveAttempt =>
        auditCYA(taxYear, request, foreignIncomeDividends, isFailed = saveAttempt.isFailure, accrualsOrCash)
      }
    }
  }

  private def auditCYA(
    taxYear: Int,
    request: DataRequest[AnyContent],
    foreignIncomeDividends: ForeignIncomeDividends,
    isFailed: Boolean,
    accrualsOrCash: Boolean
  )(implicit
    hc: HeaderCarrier
  ): Unit = {
    val auditModel = AuditModel(
      userType = request.user.affinityGroup,
      nino = request.user.nino,
      mtdItId = request.user.mtditid,
      taxYear = taxYear,
      propertyType = AuditPropertyType.ForeignProperty,
      countryCode = Country.UK.code,
      journeyName = JourneyName.ForeignIncome,
      sectionName = SectionName.ForeignIncomeDividends,
      accountingMethod = if(accrualsOrCash) Traditional else Cash,
      isUpdate = false,
      isFailed = isFailed,
      agentReferenceNumber = request.user.agentRef,
      userEnteredDetails = foreignIncomeDividends
    )

    audit.sendAuditEvent(auditModel)
  }

}
