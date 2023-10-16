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

package controllers

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.UKPropertySelect
import pages.{TotalIncomePage, UKPropertyPage}
import pages.propertyrentals.ClaimPropertyIncomeAllowancePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SummaryView
import viewmodels.summary.{TaskListItem, TaskListTag}

import javax.inject.Inject

class SummaryController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 getData: DataRetrievalAction,
                                 view: SummaryView
                               ) extends FrontendBaseController with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val propertyRentalsAbout: TaskListItem = TaskListItem(
      "summary.about",
      controllers.propertyrentals.routes.PropertyRentalsStartController.onPageLoad(taxYear),
      if (request.userAnswers.flatMap(_.get(TotalIncomePage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted
    )
    val propertyRentalsIncome: TaskListItem = TaskListItem(
      "summary.income",
      controllers.propertyrentals.routes.PropertyIncomeStartController.onPageLoad(taxYear),
      if (request.userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage)).isDefined) TaskListTag.InProgress else TaskListTag.NotStarted
    )
    val propertyRentalsAdjustments: TaskListItem = TaskListItem("summary.adjustments",
      controllers.routes.SummaryController.show(taxYear), //to change to adjustments page
      TaskListTag.NotStarted ///update based on first page
    )

    val ukPropertyRentalsRows: Seq[TaskListItem] = if(request.userAnswers.flatMap(_.get(ClaimPropertyIncomeAllowancePage)).isDefined)
      {Seq(propertyRentalsAbout, propertyRentalsIncome, propertyRentalsAdjustments)}
    else if (request.userAnswers.flatMap(_.get(UKPropertyPage)).exists(_.contains(UKPropertySelect.PropertyRentals)))
      {Seq(propertyRentalsAbout)}
    else {Seq.empty[TaskListItem]}


    Ok(view(taxYear, ukPropertyRentalsRows))
  }
}
