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

package viewmodels

import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern
case class UkAndForeignPropertyDetailsPage(
                                            taxYear: Int,
                                            individualOrAgent: String,
                                            ukPropertyTradingStartDate: LocalDate,
                                            ukPropertyAccrualsOrCash: Boolean,
                                            foreignPropertyAccrualsOrCash: Boolean,
                                            foreignPropertyTradingStartDate: LocalDate
                                          ) {
  def ukCashOrAccrualsMessageKey: String = if (ukPropertyAccrualsOrCash) "businessDetails.accruals" else "businessDetails.cash"
  def ukTradingStartDateFormatted: String = ukPropertyTradingStartDate.format(ofPattern("dd/MM/yyyy"))
  def foreignCashOrAccrualsMessageKey: String = if (foreignPropertyAccrualsOrCash) "businessDetails.accruals" else "businessDetails.cash"
  def foreignTradingStartDateFormatted: String = foreignPropertyTradingStartDate.format(ofPattern("dd/MM/yyyy"))
}