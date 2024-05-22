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

import models.{Adjustments, EsbasWithSupportingQuestions, FetchedBackendData, UserAnswers}
import pages._
import pages.adjustments._
import pages.enhancedstructuresbuildingallowance._
import play.api.libs.json.Writes
import queries.Settable

import scala.util.{Success, Try}

object PropertyPeriodSessionRecoveryExtensions {

  implicit class UserAnswersExtension(userAnswersArg: UserAnswers) {
    def updatePart[T](userAnswers: UserAnswers, page: Settable[T], value: Option[T])(implicit writes: Writes[T]): Try[UserAnswers] = {
      value.fold[Try[UserAnswers]](Success(userAnswers))(v => userAnswers.set(page, v))
    }

    def update(fetchedData: FetchedBackendData): UserAnswers = {
      for {
        ua1 <- updatePart(userAnswersArg, CapitalAllowancesForACarPage, fetchedData.capitalAllowancesForACar)
        ua2 <- updatePart(ua1, UKPropertyPage, fetchedData.propertyAbout.map(_.ukProperty.toSet))
        ua3 <- updatePart(ua2, TotalIncomePage, fetchedData.propertyAbout.map(_.totalIncome))
        ua5 <- updateAdjustmentsPages(ua3, fetchedData.adjustments)
        // Todo: When ticked implemented ua6 <- updateAllowancesPages(ua5, fetchedData.allowances)
        // Todo: When ticked implemented ua7 <- updateStructureBuildingPages(ua6, fetchedData.sbasWithSupportingQuestions)
        ua8 <- updateEnhancedStructureBuildingPages(ua5, fetchedData.esbasWithSupportingQuestions) //updateEnhancedStructureBuildingPages(ua7, fetchedData.esbasWithSupportingQuestions)
        // Todo: When ticked implemented ua9 <- updatePropertyRentalPages(ua8, fetchedData.propertyRentals)
      } yield ua8 //ua9
    }.getOrElse(userAnswersArg)

    def updateAdjustmentsPages(userAnswers: UserAnswers, maybeAdjustments: Option[Adjustments]): Try[UserAnswers] = {
      maybeAdjustments match {
        case None => Success(userAnswers)
        case Some(adjustments) => for {
          ua1 <- userAnswers.set(BalancingChargePage, adjustments.balancingCharge)
          ua2 <- ua1.set(PrivateUseAdjustmentPage, adjustments.privateUseAdjustment)
          ua3 <- ua2.set(PropertyIncomeAllowancePage, adjustments.propertyIncomeAllowance)
          ua4 <- ua3.set(RenovationAllowanceBalancingChargePage, adjustments.renovationAllowanceBalancingCharge)
          ua5 <- ua4.set(ResidentialFinanceCostPage, adjustments.residentialFinancialCost)
          ua6 <- ua5.set(UnusedResidentialFinanceCostPage, adjustments.unusedResidentialFinanceCost)
        } yield ua6
      }
    }

    def updateEnhancedStructureBuildingPages(
                                              userAnswers: UserAnswers,
                                              maybeEsbasWithSupportingQuestions: Option[EsbasWithSupportingQuestions]
                                            ): Try[UserAnswers] = {
      maybeEsbasWithSupportingQuestions match {
        case None => Success(userAnswers)
        case Some(esbasWithSupportingQuestions) =>
          for {
            ua1 <- userAnswers.set(ClaimEsbaPage, esbasWithSupportingQuestions.claimEnhancedStructureBuildingAllowance)
            ua2 <- ua1.set(EsbaClaimsPage, esbasWithSupportingQuestions.esbaClaims.getOrElse(false))
            ua3 <- updateAllEsbas(ua2, esbasWithSupportingQuestions.esbas)
          } yield ua3
      }

    }

    def updateEsba(userAnswers: UserAnswers, index: Int, esba: Esba): Try[UserAnswers] = {
      for {
        ua1 <- userAnswers.set(EsbaAddressPage(index), esba.esbaAddress)
        ua2 <- ua1.set(EsbaQualifyingDatePage(index), esba.esbaQualifyingDate)
        ua3 <- ua2.set(EsbaQualifyingAmountPage(index), esba.esbaQualifyingAmount)
        ua4 <- ua3.set(EsbaClaimPage(index), esba.esbaClaim)
      } yield ua4

    }

    def updateAllEsbas(userAnswers: UserAnswers, fetchedData: List[Esba]): Try[UserAnswers] = {
      fetchedData.zipWithIndex.foldLeft(Try(userAnswers))((acc, a) => {
        val (esba, index) = a
        acc.flatMap(ua => updateEsba(ua, index, esba))
      })
    }

  }
}
