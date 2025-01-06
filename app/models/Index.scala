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

package models

import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable

case class Index(position: Int) {

  def positionZeroIndexed: Int = position - 1

}

object Index {

  implicit val format: Format[Index] = Json.format[Index]

  private val totalCountriesInWorld: Int = 195

  implicit def indexPathBindable(implicit intBinder: PathBindable[Int]): PathBindable[Index] = new PathBindable[Index] {

    override def bind(key: String, value: String): Either[String, Index] =
      intBinder.bind(key, value) match {
        case Right(n) if n > 0 && n <= totalCountriesInWorld => Right(Index(n))
        case _                 => Left("Index binding failed")
      }

    override def unbind(key: String, value: Index): String =
      intBinder.unbind(key, value.position)
  }
}
