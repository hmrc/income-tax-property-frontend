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

package controllers.session

import models.{FetchedBackendData, UserAnswers}
import pages._
import pages.adjustments._
import pages.allowances._
import pages.enhancedstructuresbuildingallowance._
import pages.furnishedholidaylettings._
import pages.furnishedholidaylettings.income.{FhlDeductingTaxPage, FhlIsNonUKLandlordPage}
import pages.premiumlease._
import pages.propertyrentals.expenses._
import pages.propertyrentals.income._
import pages.propertyrentals.{ClaimPropertyIncomeAllowancePage, ExpensesLessThan1000Page}
import pages.structurebuildingallowance._
import play.api.libs.json.{JsObject, Reads, Writes}
import queries.Gettable

object PropertyPeriodSessionRecoveryExtensions {

  implicit class UserAnswersExtension(userAnswers: UserAnswers) {
    def update(fetchedPropertyData: FetchedBackendData): UserAnswers = {
      val fetchedData = fetchedPropertyData.fetchedData
      userAnswers.updatePage(CapitalAllowancesForACarPage, fetchedData)
        .updatePage(UKPropertyPage, fetchedData)
        .updatePage(TotalIncomePage, fetchedData)
        .updatePage(ReportPropertyIncomePage, fetchedData)
        .updateAdjustmentsPages(fetchedData)
        .updateAllowancesPages(fetchedData)
        .updateEsbaPages(fetchedData)
        .updateFhlPages(fetchedData)
        .updatePremiumLeasePages(fetchedData)
        .updatePropertyRentalPages(fetchedData)
        .updateStructureBuildingPages(fetchedData)
    }

    def updatePage[A](page: QuestionPage[A], fetchedData: JsObject)(implicit reads: Reads[A], writes: Writes[A]): UserAnswers = {
      val p: Option[A] = get(page, fetchedData)
      val r: Option[UserAnswers] = p.map(value => userAnswers.set(page, value).toOption).flatten
      r.getOrElse(userAnswers)
    }

    def updateAdjustmentsPages(fetchedData: JsObject): UserAnswers = {
      userAnswers
        .updatePage(BalancingChargePage, fetchedData)
        .updatePage(PrivateUseAdjustmentPage, fetchedData)
        .updatePage(PropertyIncomeAllowancePage, fetchedData)
        .updatePage(RenovationAllowanceBalancingChargePage, fetchedData)
        .updatePage(ResidentialFinanceCostPage, fetchedData)
        .updatePage(UnusedResidentialFinanceCostPage, fetchedData)
    }

    def updateStructuredBuildingAllowancesPages(index: Int, fetchedData: JsObject): UserAnswers = {

      val doAllDataExist = doAllSbaDataExistIn(index, fetchedData)
      val userAnswerPopulated = userAnswers
        .updatePage(StructuredBuildingAllowanceAddressPage(index), fetchedData)
        .updatePage(StructureBuildingQualifyingDatePage(index), fetchedData)
        .updatePage(StructureBuildingQualifyingAmountPage(index), fetchedData)
        .updatePage(StructureBuildingAllowanceClaimPage(index), fetchedData)

      if (doAllDataExist) {
        userAnswerPopulated.updateStructuredBuildingAllowancesPages(index + 1, fetchedData)
      } else {
        userAnswerPopulated
      }
    }

    private def doAllSbaDataExistIn(index: Int, fetchedData: JsObject): Boolean = {

      val allPresent: Option[Unit] = for {
        _ <- get(StructuredBuildingAllowanceAddressPage(index), fetchedData)
        _ <- get(StructureBuildingQualifyingDatePage(index), fetchedData)
        _ <- get(StructureBuildingQualifyingAmountPage(index), fetchedData)
        _ <- get(StructureBuildingAllowanceClaimPage(index), fetchedData)
      } yield ()

      allPresent.isDefined
    }

    def updateAllowancesPages(fetchedData: JsObject): UserAnswers = {
      userAnswers.updatePage(AnnualInvestmentAllowancePage, fetchedData)
        .updatePage(BusinessPremisesRenovationPage, fetchedData)
        .updatePage(ElectricChargePointAllowancePage, fetchedData)
        .updatePage(OtherCapitalAllowancePage, fetchedData)
        .updatePage(ReplacementOfDomesticGoodsPage, fetchedData)
        .updatePage(ZeroEmissionCarAllowancePage, fetchedData)
        .updatePage(ZeroEmissionGoodsVehicleAllowancePage, fetchedData)
    }

    def updatePropertyRentalPages(fetchedData: JsObject): UserAnswers = {
      userAnswers.updatePage(ConsolidatedExpensesPage, fetchedData)
        .updatePage(CostsOfServicesProvidedPage, fetchedData)
        .updatePage(LoanInterestPage, fetchedData)
        .updatePage(OtherAllowablePropertyExpensesPage, fetchedData)
        .updatePage(OtherProfessionalFeesPage, fetchedData)
        .updatePage(PropertyBusinessTravelCostsPage, fetchedData)
        .updatePage(RentsRatesAndInsurancePage, fetchedData)
        .updatePage(RepairsAndMaintenanceCostsPage, fetchedData)
        .updatePage(DeductingTaxPage, fetchedData)
        .updatePage(IncomeFromPropertyRentalsPage, fetchedData)
        .updatePage(IsNonUKLandlordPage, fetchedData)
        .updatePage(OtherIncomeFromPropertyPage, fetchedData)
        .updatePage(ReversePremiumsReceivedPage, fetchedData)
        .updatePage(ClaimPropertyIncomeAllowancePage, fetchedData)
        .updatePage(ExpensesLessThan1000Page, fetchedData)
    }

    def updateFhlPages(fetchedData: JsObject): UserAnswers = {
      userAnswers.updatePage(FhlDeductingTaxPage, fetchedData: JsObject)
        .updatePage(FhlIsNonUKLandlordPage, fetchedData: JsObject)
        .updatePage(FhlJointlyLetPage, fetchedData: JsObject)
        .updatePage(FhlMainHomePage, fetchedData: JsObject)
        .updatePage(FhlMoreThanOnePage, fetchedData: JsObject)
        .updatePage(FhlReliefOrExpensesPage, fetchedData: JsObject)
    }

    def updateStructureBuildingPages(fetchedData: JsObject): UserAnswers = {
      userAnswers
        .updatePage(ClaimStructureBuildingAllowancePage, fetchedData)
        .updatePage(SbaClaimsPage, fetchedData)
        .updatePage(SbaRemoveConfirmationPage, fetchedData)
        .updateStructuredBuildingAllowancesPages(0, fetchedData)
    }

    def updateEsbaPages(fetchedData: JsObject): UserAnswers = {
      userAnswers
        .updatePage(EsbaClaimsPage, fetchedData)
        .updatePage(EsbaRemoveConfirmationPage, fetchedData)
        .updatePage(ClaimEsbaPage, fetchedData)
        .updateEsbaPages(0, fetchedData)
    }

    def updateEsbaPages(index: Int, fetchedData: JsObject): UserAnswers = {

      val doAllDataExist = doAllEsbaDataExistIn(index, fetchedData)
      val userAnswerPopulated = userAnswers
        .updatePage(EsbaQualifyingDatePage(index), fetchedData)
        .updatePage(EsbaQualifyingAmountPage(index), fetchedData)
        .updatePage(EsbaClaimPage(index), fetchedData)
        .updatePage(EsbaClaimAmountPage(index), fetchedData)
        .updatePage(EsbaAddressPage(index), fetchedData)

      if (doAllDataExist) {
        userAnswerPopulated.updateStructuredBuildingAllowancesPages(index + 1, fetchedData)
      } else {
        userAnswerPopulated
      }
    }

    private def doAllEsbaDataExistIn(index: Int, fetchedData: JsObject): Boolean = {

      val allPresent: Option[Unit] = for {
        _ <- get(EsbaQualifyingDatePage(index), fetchedData)
        _ <- get(EsbaQualifyingAmountPage(index), fetchedData)
        _ <- get(EsbaClaimPage(index), fetchedData)
        _ <- get(EsbaClaimAmountPage(index), fetchedData)
        _ <- get(EsbaAddressPage(index), fetchedData)
      } yield ()

      allPresent.isDefined
    }

    def updatePremiumLeasePages(fetchedData: JsObject): UserAnswers = {
      userAnswers.updatePage(CalculatedFigureYourselfPage, fetchedData)
        .updatePage(LeasePremiumPaymentPage, fetchedData)
        .updatePage(PremiumsGrantLeasePage, fetchedData)
        .updatePage(ReceivedGrantLeaseAmountPage, fetchedData)
        .updatePage(YearLeaseAmountPage, fetchedData)
    }

    private def get[A](page: Gettable[A], data: JsObject)(implicit rds: Reads[A]): Option[A] =
      Reads.optionNoError(Reads.at(page.path)(rds)).reads(data).getOrElse(None)
  }
}
