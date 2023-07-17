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

package viewmodels.govuk

import uk.gov.hmrc.govukfrontend.views.viewmodels.button.Button
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content

object button extends ButtonFluency

trait ButtonFluency {

  object ButtonViewModel {

    def apply(content: Content): Button =
      Button(
        element = Some("button"),
        content = content
      )
  }

  implicit class FluentButton(button: Button) {

    def asLink(href: String): Button =
      button.copy (
        element = Some("a"),
        href    = Some(href)
      )

    def asInput(inputType: String): Button =
      button.copy (
        element   = Some("input"),
        inputType = Some(inputType)
      )

    def withName(name: String): Button =
      button.copy(name = Some(name))

    def withCssClass(newClass: String): Button =
      button.copy(classes = s"${button.classes} $newClass")

    def withAttribute(attribute: (String, String)): Button =
      button.copy(attributes = button.attributes + attribute)

    def disabled(): Button =
      button.copy(disabled = true)

    def preventingDoubleClick(): Button =
      button.copy(preventDoubleClick = Some(true))

    def asStartButton(): Button =
      button.copy(isStartButton = true)

    def asSecondaryButton(): Button =
      withCssClass("govuk-button--secondary")

    def asWarningButton(): Button =
      withCssClass("govuk-button--warning")
  }
}
