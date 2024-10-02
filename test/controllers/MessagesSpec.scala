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

package controllers

import base.SpecBase
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables

import scala.io.Source

class MessagesSpec extends SpecBase {

  private val exclusionKeys: Set[String] = Set(
    "annualInvestmentAllowance.details.content.link.href",
    "businessPremisesRenovation.details.content.line2.href",
    "capitalAllowancesForACar.details.link.href",
    "claimPropertyIncomeAllowance.details.content.link.href",
    "claimPropertyIncomeAllowance.error.required.agent",
    "claimPropertyIncomeAllowance.error.required.individual",
    "claimPropertyIncomeAllowance.summary.no",
    "claimPropertyIncomeAllowance.summary.yes",
    "claimStructureBuildingAllowance.details.content.link.href",
    "deductingTax.details.content.line1.agent",
    "deductingTax.details.content.link.href",
    "electricChargePointAllowance.details.content.link.href",
    "error.year.invalid",
    "expensesLessThan1000.p2.link.href",
    "expensesStart.details.content5.link.href",
    "expensesStart.details.content7.link.href",
    "propertyRentalIncome.para2.Rentals.agent",
    "propertyRentalIncome.para2.Rentals.individual",
    "propertyRentalIncome.para2.RentalsRentARoom.agent",
    "propertyRentalIncome.para2.RentalsRentARoom.individual",
    "index.guidance",
    "index.heading",
    "index.title",
    "jointlyLet.title",
    "journeyRecovery.continue.guidance",
    "journeyRecovery.startAgain.guidance",
    "otherAllowablePropertyExpenses.details.link.href",
    "otherAllowablePropertyExpenses.details.link.text",
    "propertyIncomeAllowance.details.content3.link.href",
    "propertyRentalsStart.details.p4.link.href",
    "receivedGrantLeaseAmount.error.nonNumeric.agent",
    "receivedGrantLeaseAmount.error.nonNumeric.individual",
    "receivedGrantLeaseAmount.error.outOfRange.agent",
    "receivedGrantLeaseAmount.error.outOfRange.individual",
    "receivedGrantLeaseAmount.error.required.agent",
    "receivedGrantLeaseAmount.error.required.individual",
    "receivedGrantLeaseAmount.error.twoDecimalPlaces.agent",
    "receivedGrantLeaseAmount.error.twoDecimalPlaces.individual",
    "rentalsAndRentARoom.income.nonUKResidentialLandlord.change.hidden",
    "rentalsAndRentARoom.income.nonUKResidentialLandlord.checkYourAnswersLabel",
    "rentalsAndRentARoom.income.nonUKResidentialLandlord.error.required",
    "rentalsAndRentARoom.income.nonUKResidentialLandlord.heading",
    "rentalsAndRentARoom.income.nonUKResidentialLandlord.title",
    "rentalsAndRentARoom.start.heading.agent",
    "rentalsAndRentARoom.start.heading.individual",
    "rentalsAndRentARoom.start.p.agent",
    "rentalsAndRentARoom.start.p.individual",
    "rentalsAndRentARoom.start.title.agent",
    "rentalsAndRentARoom.start.title.individual",
    "replacementOfDomesticGoods.details.content.line2.href",
    "reportPropertyIncome.change.hidden",
    "reportPropertyIncome.checkYourAnswersLabel.agent",
    "reportPropertyIncome.checkYourAnswersLabel.individual",
    "reportPropertyIncome.details.input.header.agent",
    "reportPropertyIncome.details.input.header.individual",
    "reportPropertyIncome.details.input.hint.agent",
    "reportPropertyIncome.details.input.hint.individual",
    "reportPropertyIncome.details.input.noText.agent",
    "reportPropertyIncome.details.input.noText.individual",
    "reportPropertyIncome.details.input.yesText.agent",
    "reportPropertyIncome.details.input.yesText.individual",
    "reportPropertyIncome.error.required.agent",
    "reportPropertyIncome.error.required.individual",
    "reportPropertyIncome.heading",
    "reportPropertyIncome.title",
    "reversePremiumsReceived.details.line2.link.href",
    "site.govuk",
    "structureBuildingQualifyingDate.details.content2.link.href",
    "summary.combined.heading",
    "summary.rentARoom.heading",
    "ukRentARoomJointlyLet.checkYourAnswersLabel",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.bullet1",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.bullet2",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.bullet3",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.change.hidden.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.checkYourAnswersLabel.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.error.nonNumeric.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.error.outOfRange.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.error.required.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.error.twoDecimalPlaces.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.heading.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.label.individual",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.para1",
    "ukrentaroom.expenses.residentialPropertyFinanceCosts.title.individual",
    "unusedResidentialFinanceCost.details.content.link.href",
    "zeroEmissionCarAllowance.details.link.href"
  )

  "MessagesSpec" - {
    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isAgent = true).build()
    val allLanguages: Map[String, Map[String, String]] = definedMessages(application)

    val englishKeys = allLanguages("en").keys.toSet
    val welshKeys = allLanguages("cy").keys.toSet

    "ensure all keys in the default file have corresponding translations in Welsh except those in the exclusion list" in {
      val missingKeys = englishKeys.diff(exclusionKeys).diff(welshKeys)

      withClue(s"Missing keys in Welsh: ${missingKeys.mkString(", ")}") {
        missingKeys mustBe empty
      }
    }

  }

  "Messages files" - {
    val messageFiles = Tables.Table[String, String](
      ("filePath", "description"),
      ("conf/messages.en", "English messages file"),
      ("conf/messages.cy", "Welsh messages file")
    )

    forAll(messageFiles) { (filePath: String, description: String) =>
      s"not contain duplicate keys in $description" in {
        val source = Source.fromFile(filePath)
        val lines =
          try source.getLines().toList
          finally source.close()

        val keys = lines.filterNot(key => key.isBlank || key.trim.startsWith("#")).map { line =>
          line.split("=").head.trim
        }

        val duplicates = keys.groupBy(identity).collect {
          case (key, occurrences) if occurrences.size > 1 => key
        }

        duplicates mustBe empty
      }
    }
  }

}
