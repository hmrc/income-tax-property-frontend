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

package models

import play.api.mvc.{JavascriptLiteral, QueryStringBindable}

sealed trait Mode

case object CheckMode extends Mode
case object NormalMode extends Mode

object Mode {

  val normalMode = "NormalMode"
  val checkMode = "CheckMode"

  implicit val jsLiteral: JavascriptLiteral[Mode] = new JavascriptLiteral[Mode] {
    override def to(value: Mode): String = value match {
      case NormalMode => "NormalMode"
      case CheckMode => "CheckMode"
    }
  }

  implicit val queryStringBinder: QueryStringBindable[Mode] = new QueryStringBindable[Mode] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Mode]] = {
      params.get(key).flatMap(_.headOption) match {
        case Some(`normalMode`) => Some(Right(NormalMode))
        case Some(`checkMode`) => Some(Right(CheckMode))
        case Some(x) => {
          Some(Left(x))
        }
        case None => None
      }
    }

    override def unbind(key: String, value: Mode): String = s"$key=${value.toString}"
  }

}
