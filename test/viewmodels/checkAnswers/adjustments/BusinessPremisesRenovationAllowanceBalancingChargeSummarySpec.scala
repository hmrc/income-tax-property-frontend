package viewmodels.checkAnswers.adjustments

import models.{CheckMode, PropertyType, RenovationAllowanceBalancingCharge, Rentals, RentalsRentARoom, UserAnswers}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessagesApi
import controllers.rentalsandrentaroom.adjustments.routes.BusinessPremisesRenovationBalancingChargeController
import pages.adjustments.RenovationAllowanceBalancingChargePage
import viewmodels.checkAnswers.FormatUtils.{bigDecimalCurrency, keyCssClass, valueCssClass}
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.util.Locale

class BusinessPremisesRenovationAllowanceBalancingChargeSummarySpec extends AnyWordSpec with Matchers {
  val taxYear = 2024
  implicit val messages: Messages = stubMessagesApi().preferred(List(Lang(Locale.ENGLISH)))
  val propertyType: PropertyType = RentalsRentARoom
  val businessPremisesRenovationAllowanceBalancingChargeAmount = 11.22
  val userAnswersId = "some-id"

  "BusinessPremisesRenovationAllowanceBalancingChargeSummary" should {
    "create the correct summary list row" when {
      "the charge exists" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(RenovationAllowanceBalancingChargePage(propertyType),
          RenovationAllowanceBalancingCharge(
            isRenovationAllowanceBalancingCharge = true,
            renovationAllowanceBalancingChargeAmount = Some(businessPremisesRenovationAllowanceBalancingChargeAmount)
          )
        ).get
        val businessPremisesRenovationAllowanceBalancingChargeRow =
          BusinessPremisesRenovationAllowanceBalancingChargeSummary.row(taxYear, userAnswers)

        businessPremisesRenovationAllowanceBalancingChargeRow shouldBe Some(
          SummaryListRowViewModel(
            key =
              KeyViewModel("businessPremisesRenovationBalancingCharge.checkYourAnswersLabel").withCssClass(keyCssClass),
            value = ValueViewModel(bigDecimalCurrency(businessPremisesRenovationAllowanceBalancingChargeAmount)).withCssClass(valueCssClass),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                BusinessPremisesRenovationBalancingChargeController.onPageLoad(taxYear, CheckMode).url
              )
                .withVisuallyHiddenText(messages("businessPremisesRenovationBalancingCharge.change.hidden"))
            )
          )
        )
      }

      "the charge is empty" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId).set(RenovationAllowanceBalancingChargePage(propertyType),
          RenovationAllowanceBalancingCharge(
            isRenovationAllowanceBalancingCharge = false,
            renovationAllowanceBalancingChargeAmount = None
          )
        ).get
        val businessPremisesRenovationAllowanceBalancingChargeRow =
          BusinessPremisesRenovationAllowanceBalancingChargeSummary.row(taxYear, userAnswers)
        businessPremisesRenovationAllowanceBalancingChargeRow.map(_.value) shouldBe Some(
          ValueViewModel("site.no").withCssClass(valueCssClass)
        )
      }

      "the question has not been answered" in {
        val userAnswers: UserAnswers = UserAnswers(userAnswersId)
        val businessPremisesRenovationAllowanceBalancingChargeRow =
          BusinessPremisesRenovationAllowanceBalancingChargeSummary.row(taxYear, userAnswers)

        businessPremisesRenovationAllowanceBalancingChargeRow shouldBe None
      }
    }
  }

}
