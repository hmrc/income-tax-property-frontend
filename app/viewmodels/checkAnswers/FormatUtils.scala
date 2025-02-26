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

package viewmodels.checkAnswers

object FormatUtils {

  val keyCssClass = "govuk-!-width-one-half"
  val keyAlignLeftCssClass = "govuk-!-width-one-half-text-align-left"
  val valueCssClass = "govuk-!-text-align-right"
  val selectCountriesValueCssClass = "govuk-!-padding-left-6"
  val selectCountriesValueAlignLeftCssClass = "govuk-!-padding-left-6-text-align-left"

  def bigDecimalCurrency(value: BigDecimal, currencySymbol: String = "£"): String =
    currencySymbol + f"$value%1.2f".replace(".00", "")
      .replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",")

}
