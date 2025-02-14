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

package pages.foreign.income

import models.{ForeignProperty, UserAnswers}
import models.ForeignTotalIncomeUtils.isTotalIncomeUnder85K
import pages.foreign.expenses.ConsolidatedOrIndividualExpensesPage
import pages.PageConstants.incomePath
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case class ForeignPropertyRentalIncomePage(countryCode:String) extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ incomePath(ForeignProperty) \ countryCode.toUpperCase \ toString

  override def toString: String = "rentIncome"

  override def cleanup(value: Option[BigDecimal], userAnswers: UserAnswers): Try[UserAnswers] =
    if (isTotalIncomeUnder85K(userAnswers, countryCode)) {
      super.cleanup(value, userAnswers)
    } else if (
      userAnswers.get(ConsolidatedOrIndividualExpensesPage(countryCode)).fold(false)(data => data.consolidatedOrIndividualExpensesYesNo)
    ) {
      userAnswers.remove(ConsolidatedOrIndividualExpensesPage(countryCode))
    } else {
      super.cleanup(value, userAnswers)
    }
}

