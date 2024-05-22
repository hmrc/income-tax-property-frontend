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

package service

import connectors.PropertySubmissionConnector
import connectors.error.ApiError
import models.{FetchedPropertyData, User}
import play.api.Logging
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyPeriodSubmissionService @Inject()(propertySubmissionConnector: PropertySubmissionConnector)
                                               (implicit val ec: ExecutionContext) extends Logging {
  def getPropertyPeriodicSubmission(taxYear: Int, user: User)(implicit hc: HeaderCarrier): Future[Either[ApiError, FetchedPropertyData]] = {
    propertySubmissionConnector.getPropertyPeriodicSubmission(taxYear, user.mtditid, user).map({
      case Left(_) => {
        logger.error("PropertyPeriodicSubmissionConnector endpoint is unreachable.")
        Right(FetchedPropertyData(new JsObject(Map())))
      }
      case Right(r) => Right(r)
    })
  }
}
