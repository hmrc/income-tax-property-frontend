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

package controllers.actions

import models.{RentARoom, UserAnswers}
import models.requests.DataRequest
import pages.QuestionPage
import pages.ukrentaroom.allowances.RaRAllowancesCompletePage
import play.api.mvc.{ActionBuilder, ActionRefiner, AnyContent, Call, Result}
import controllers.about.routes._
import controllers.adjustments.routes._
import controllers.allowances.routes._
import controllers.enhancedstructuresbuildingallowance.routes._
import controllers.premiumlease.routes._
import controllers.propertyrentals.expenses.routes._
import controllers.propertyrentals.income.routes._
import controllers.propertyrentals.routes._
import controllers.rentalsandrentaroom.adjustments.routes._
import controllers.rentalsandrentaroom.routes
import controllers.routes._
import controllers.structuresbuildingallowance.routes._
import controllers.ukrentaroom.adjustments.routes._
import controllers.ukrentaroom.allowances.routes._
import controllers.ukrentaroom.expenses.routes._
import controllers.ukrentaroom.routes._
import models.TotalIncome.{Between, Over, Under}
import models._
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.premiumlease.{CalculatedFigureYourselfPage, PremiumForLeasePage}
import pages.propertyrentals._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income.{IncomeSectionFinishedPage, _}
import pages.rentalsandrentaroom.adjustments.BusinessPremisesRenovationAllowanceBalancingChargePage
import pages.structurebuildingallowance._
import pages.ukrentaroom._
import pages.ukrentaroom.adjustments._
import pages.ukrentaroom.allowances._
import pages.ukrentaroom.expenses._
import play.api.mvc.Call
import play.api.mvc.Results.Redirect
import queries.Gettable

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

class CYAAction(implicit ec: ExecutionContext) extends ActionRefiner[DataRequest, DataRequest] {
  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DataRequest[A]]] = ???
//    journeyName: String
//    ,
//    propertyType: PropertyType
//    request.userAnswers match {
//      case None =>
//        Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
//      case Some(data) =>
//        Future.successful(Right(DataRequest(request.request, request.userId, request.user, data)))
//    }

  override protected def executionContext: ExecutionContext = ec

}
