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

sealed abstract class TaskListTag(comment: String)

object TaskListTag {
  case object InProgress extends TaskListTag(comment = "Section started from scratch by user but not completed")
  case object NotStarted extends TaskListTag(comment = "No user entered data saved and no pre-populated data")
  case object Completed extends TaskListTag(comment = "All fields completed, or marked as completed by the user.")
}