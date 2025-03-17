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

package controllers.session

import com.google.inject.Inject
import controllers.session.PropertyPeriodSessionRecoveryExtensions._
import models.backend.ServiceError
import models.requests.OptionalDataRequest
import models.{FetchedPropertyData, UserAnswers}
import play.api.mvc.{AnyContent, Result}
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
trait SessionRecovery {
  def withUpdatedData(taxYear: Int)(
    block: OptionalDataRequest[AnyContent] => Future[Result]
  )(implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[Result]
}

class PropertyPeriodSessionRecovery @Inject() (
  propertyPeriodSubmissionService: PropertySubmissionService,
  sessionRepository: SessionRepository
) extends SessionRecovery {
  override def withUpdatedData(taxYear: Int)(
    block: OptionalDataRequest[AnyContent] => Future[Result]
  )(implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    val basicUserAnswers = request.userAnswers.getOrElse(UserAnswers(request.userId))
    for {
      ukFetchedData      <- propertyPeriodSubmissionService.getUKPropertySubmission(taxYear, request.user)
      foreignFetchedData <- propertyPeriodSubmissionService.getForeignPropertySubmission(taxYear, request.user)
      fetchedData = mergeFetchedData(ukFetchedData, foreignFetchedData)
      currentUserAnswersMaybe <- sessionRepository
                                   .get(request.userId)

      updatedUserAnswers <- {
        fetchedData match {
          case Right(fetchedUserAnswersData) =>
            currentUserAnswersMaybe.fold {
              val updated = basicUserAnswers.update(fetchedUserAnswersData)
              sessionRepository
                .set(
                  updated
                )
                .map(_ => updated)
            }(ua => Future.successful(ua))
          case Left(_) =>
            Future.successful(basicUserAnswers)
        }
      }
      blockResult <-
        block(OptionalDataRequest(request.request, request.userId, request.user, Some(updatedUserAnswers)))
    } yield blockResult
  }

  def mergeFetchedData(
    eitherUkFetchedData: Either[ServiceError, FetchedPropertyData],
    eitherForeignFetchedData: Either[ServiceError, FetchedPropertyData]
  ): Either[ServiceError, FetchedPropertyData] =
    eitherUkFetchedData.fold(
      _ => eitherForeignFetchedData,
      ukFetchedData =>
        eitherForeignFetchedData.fold(
          _ => Right(ukFetchedData),
          foreignFetchedData => Right(ukFetchedData.copy(foreignPropertyData = foreignFetchedData.foreignPropertyData))
        )
    )

}
