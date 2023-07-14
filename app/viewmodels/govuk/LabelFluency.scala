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

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import viewmodels.LabelSize

object label extends LabelFluency

trait LabelFluency {

  object LabelViewModel {

    def apply(content: Content): Label =
      Label(content = content)
  }

  implicit class FluentLabel(label: Label) {

    def asPageHeading(size: LabelSize = LabelSize.ExtraLarge): Label =
      label
        .copy(isPageHeading = true)
        .withCssClass(size.toString)

    def withCssClass(className: String): Label =
      label.copy(classes = s"${label.classes} $className")

    def withAttribute(attribute: (String, String)): Label =
      label.copy(attributes = label.attributes + attribute)

    def forAttr(attr: String): Label =
      label.copy(forAttr = Some(attr))
  }
}
