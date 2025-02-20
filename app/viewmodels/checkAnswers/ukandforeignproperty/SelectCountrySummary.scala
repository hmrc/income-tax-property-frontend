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

package viewmodels.checkAnswers.ukandforeignproperty

import controllers.ukandforeignproperty.routes
import models.{CheckMode, Index}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.keyCssClass
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object SelectCountrySummary  {

  def row(taxYear: Int, index: Index, name: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = KeyViewModel(HtmlFormat.escape(name).toString).withCssClass(keyCssClass),
      value = ValueViewModel(HtmlFormat.escape(messages("countriesRentedProperty.staticContent")).toString),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.SelectCountryController.onPageLoad(taxYear, Index(index.position), CheckMode).url
        ).withVisuallyHiddenText(messages("selectCountry.change.hidden")),
        ActionItemViewModel("site.remove", routes.RemoveCountryController.onPageLoad(taxYear, index, CheckMode).url)
      ),
      actionsCss = "w-25"
    )
}
