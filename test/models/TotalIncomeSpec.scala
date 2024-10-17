package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class TotalIncomeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TotalIncome" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TotalIncome.values.toSeq)

      forAll(gen) {
        totalIncome =>

          JsString(totalIncome.toString).validate[TotalIncome].asOpt.value mustEqual totalIncome
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TotalIncome.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TotalIncome] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TotalIncome.values.toSeq)

      forAll(gen) {
        totalIncome =>

          Json.toJson(totalIncome) mustEqual JsString(totalIncome.toString)
      }
    }
  }
}
