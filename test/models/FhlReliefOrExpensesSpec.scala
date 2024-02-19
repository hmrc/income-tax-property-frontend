package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class FhlReliefOrExpensesSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "FhlReliefOrExpenses" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(FhlReliefOrExpenses.values.toSeq)

      forAll(gen) {
        fhlReliefOrExpenses2 =>

          JsString(fhlReliefOrExpenses2.toString).validate[FhlReliefOrExpenses].asOpt.value mustEqual fhlReliefOrExpenses2
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!FhlReliefOrExpenses.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[FhlReliefOrExpenses] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(FhlReliefOrExpenses.values.toSeq)

      forAll(gen) {
        fhlReliefOrExpenses =>

          Json.toJson(fhlReliefOrExpenses) mustEqual JsString(fhlReliefOrExpenses.toString)
      }
    }
  }
}
