package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ConsolidatedOrIndividualExpensesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ConsolidatedOrIndividualExpenses" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ConsolidatedOrIndividualExpenses.values.toSeq)

      forAll(gen) {
        consolidatedOrIndividualExpenses =>

          JsString(consolidatedOrIndividualExpenses.toString).validate[ConsolidatedOrIndividualExpenses].asOpt.value mustEqual consolidatedOrIndividualExpenses
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ConsolidatedOrIndividualExpenses.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ConsolidatedOrIndividualExpenses] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ConsolidatedOrIndividualExpenses.values.toSeq)

      forAll(gen) {
        consolidatedOrIndividualExpenses =>

          Json.toJson(consolidatedOrIndividualExpenses) mustEqual JsString(consolidatedOrIndividualExpenses.toString)
      }
    }
  }
}
