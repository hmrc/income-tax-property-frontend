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

package connectors

import connectors.error.{ApiError, MultiErrorsBody, SingleErrorBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HttpResponse

trait Parser {

  protected val parserName: String

  def badSuccessJsonResponse[Response]: Either[ApiError, Response] = {
    Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
  }

  def handleError[Response](response: HttpResponse, status: Int): Either[ApiError, Response] = {
    try {
      val json = response.json
      lazy val singleErrorBody = json.asOpt[SingleErrorBody]
      lazy val multiErrorsBody = json.asOpt[MultiErrorsBody]

      (singleErrorBody, multiErrorsBody) match {
        case (Some(error), _) => Left(ApiError(status, error))
        case (_, Some(error)) => Left(ApiError(status, error))
        case _ => Left(ApiError(status, SingleErrorBody.parsingError))
      }
    } catch {
      case _: Exception => Left(ApiError(status, SingleErrorBody.parsingError))
    }
  }
}
