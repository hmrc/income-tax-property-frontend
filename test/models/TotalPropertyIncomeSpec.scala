package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class TotalPropertyIncomeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "TotalPropertyIncome" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(TotalPropertyIncome.values.toSeq)

      forAll(gen) {
        totalPropertyIncome =>

          JsString(totalPropertyIncome.toString).validate[TotalPropertyIncome].asOpt.value mustEqual totalPropertyIncome
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!TotalPropertyIncome.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[TotalPropertyIncome] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(TotalPropertyIncome.values.toSeq)

      forAll(gen) {
        totalPropertyIncome =>

          Json.toJson(totalPropertyIncome) mustEqual JsString(totalPropertyIncome.toString)
      }
    }
  }
}
