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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.backlink.BackLink

object backlink extends BackLinkFluency

trait BackLinkFluency {

  object BackLinkViewModel {

    def apply(href: String)(implicit messages: Messages): BackLink =
      BackLink(
        href    = href,
        content = Text(messages("site.back"))
      )
  }

  implicit class FluentBackLink(backLink: BackLink) {

    def withCssClass(newClass:String): BackLink =
      backLink.copy(classes = s"${backLink.classes} $newClass")

    def withAttribute(attribute: (String, String)): BackLink =
      backLink.copy(attributes = backLink.attributes + attribute)
  }
}
