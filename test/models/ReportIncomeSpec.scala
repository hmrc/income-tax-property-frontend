package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ReportIncomeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ReportIncome" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ReportIncome.values.toSeq)

      forAll(gen) {
        reportIncome =>

          JsString(reportIncome.toString).validate[ReportIncome].asOpt.value mustEqual reportIncome
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ReportIncome.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ReportIncome] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ReportIncome.values.toSeq)

      forAll(gen) {
        reportIncome =>

          Json.toJson(reportIncome) mustEqual JsString(reportIncome.toString)
      }
    }
  }
}
