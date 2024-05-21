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
import models.UserAnswers
import models.requests.OptionalDataRequest
import play.api.mvc.{AnyContent, Result}
import repositories.SessionRepository
import service.PropertySubmissionService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class PropertyPeriodSessionRecovery @Inject()(
                                               propertyPeriodSubmissionService: PropertySubmissionService,
                                               sessionRepository: SessionRepository
                                             ) {
  def withUpdatedData(taxYear: Int)(block: => Future[Result])
                     (implicit request: OptionalDataRequest[AnyContent], ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    val currentUserAnswers = request
      .userAnswers
      .getOrElse(
        UserAnswers(request.userId)
      )

    for {
      fetchedData <- propertyPeriodSubmissionService.getPropertySubmission(taxYear, request.user)
      _ <- fetchedData match {
        case Right(fetchedUserAnswersData) =>
          sessionRepository.set(
            currentUserAnswers.update(fetchedUserAnswersData)
          )
        case Left(_) => sessionRepository.set(currentUserAnswers)
      }
      blockResult <- block
    } yield blockResult
  }
}
