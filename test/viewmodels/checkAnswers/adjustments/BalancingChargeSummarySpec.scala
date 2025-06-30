/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.checkAnswers.adjustments

import models.{BalancingCharge, CheckMode, PropertyType, Rentals, UserAnswers}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.adjustments.BalancingChargePage
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import controllers.adjustments.routes
import java.util.Locale
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

class BalancingChargeSummarySpec extends AnyWordSpec with Matchers {

  val taxYear = 2024
  implicit val messages: Messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))
  val propertyType: PropertyType = Rentals
  val balancingChargeAmount = 11.22
  val userAnswersId = "some-id"

  "BalancingChargeSummary" should {
    "create the correct summary list row" when {
      "the charge exists" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(BalancingChargePage(propertyType),
          BalancingCharge(isBalancingCharge = true, balancingChargeAmount = Some(balancingChargeAmount))).get
        val balancingChargeSummaryRow: Option[SummaryListRow] = BalancingChargeSummary.row(taxYear, userAnswers, propertyType)
        balancingChargeSummaryRow shouldBe Some(
          SummaryListRowViewModel(
             key = KeyViewModel("balancingCharge.checkYourAnswersLabel").withCssClass(keyCssClass),
             value = ValueViewModel(bigDecimalCurrency(balancingChargeAmount)).withCssClass(valueCssClass),
             actions = Seq(
                 ActionItemViewModel("site.change", routes.BalancingChargeController.onPageLoad(taxYear, CheckMode, propertyType).url)
             .withVisuallyHiddenText(messages("privateUseAdjustment.change.hidden"))
         ))
        )
      }
      "the charge is empty" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(BalancingChargePage(propertyType),
          BalancingCharge(isBalancingCharge = false, None)).get
        val balancingChargeSummaryRow: Option[SummaryListRow] = BalancingChargeSummary.row(taxYear, userAnswers, propertyType)
        balancingChargeSummaryRow.map(_.value) shouldBe Some(ValueViewModel("site.no").withCssClass(valueCssClass))
      }
      "the question has not been answered" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        val balancingChargeSummaryRow: Option[SummaryListRow] = BalancingChargeSummary.row(taxYear, userAnswers, propertyType)
        balancingChargeSummaryRow shouldBe None
      }
    }
  }
}
