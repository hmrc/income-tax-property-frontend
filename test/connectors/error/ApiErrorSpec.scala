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

package connectors.error

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.SERVICE_UNAVAILABLE
import play.api.libs.json.{JsObject, Json}

class ApiErrorSpec extends AnyWordSpec with Matchers {

  private val jsonModel: JsObject = Json.obj(
    "code" -> "SERVER_ERROR",
    "reason" -> "Service is unavailable"
  )
  private val errorsJsModel: JsObject = Json.obj(
    "failures" -> Json.arr(
      Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "The service is currently unavailable"),
      Json.obj("code" -> "INTERNAL_SERVER_ERROR", "reason" -> "The service is currently facing issues.")
    )
  )

  "The Error" should {
    "parse to Json" in {
      val underTest = ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("SERVER_ERROR", "Service is unavailable"))

      underTest.toJson shouldBe jsonModel
    }

    "parse to Json for multiple errors" in {
      val underTest = ApiError(SERVICE_UNAVAILABLE, MultiErrorsBody(Seq(
        SingleErrorBody("SERVICE_UNAVAILABLE", "The service is currently unavailable"),
        SingleErrorBody("INTERNAL_SERVER_ERROR", "The service is currently facing issues.")
      )))

      underTest.toJson shouldBe errorsJsModel
    }
  }
}
