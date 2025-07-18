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

package utils

import play.api.i18n.Messages

import java.time.format.DateTimeFormatter

object DateTimeFormats {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def localDateTimeFormatter()(implicit messages: Messages):DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", messages.lang.locale)

  def localDateTimeAbbreviatedMonthFormatter()(implicit messages: Messages):DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", messages.lang.locale)

  val dateTimeHintFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d M yyyy")

}
