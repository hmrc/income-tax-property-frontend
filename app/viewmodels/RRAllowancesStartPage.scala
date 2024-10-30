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

package viewmodels

import models.NormalMode

case class RRAllowancesStartPage(taxYear: Int, individualOrAgent: String, cashOrAccruals: Boolean) {
  def cashOrAccrualsMessageKey: String = if (cashOrAccruals) "businessDetails.accruals" else "businessDetails.cash"

  def nextPageUrl: String = if (cashOrAccruals) {
    controllers.ukrentaroom.allowances.routes.RaRZeroEmissionCarAllowanceController.onPageLoad(taxYear, NormalMode).url
  } else {
    controllers.ukrentaroom.allowances.routes.RaRCapitalAllowancesForACarController.onPageLoad(taxYear, NormalMode).url
  }
}
