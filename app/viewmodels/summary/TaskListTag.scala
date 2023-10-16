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

package viewmodels.summary

abstract class TaskListTag(message: String, cssClass: String)

object TaskListTag {
  case object InProgress extends TaskListTag("inProgress", "govuk-tag--blue")
  case object NotStarted extends TaskListTag("notStarted", "govuk-tag--grey")
  case object Completed extends TaskListTag("completed", "govuk-tag--turquoise")
}