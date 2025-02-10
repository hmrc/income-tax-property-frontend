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

package pages.ukandforeignproperty

import models.IncomeSourcePropertyType.ForeignProperty
import models.backend.PropertyDetails
import models.{UKPropertySelect, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.foreign.ForeignSelectCountriesCompletePage
import pages.{AboutPropertyCompletePage, ReportPropertyIncomePage, SummaryPage, UKPropertyPage}
import service.{BusinessService, CYADiversionService, ForeignCYADiversionService}
import viewmodels.summary.{TaskListItem, TaskListTag}

import java.time.LocalDate
import scala.concurrent.Future

class UkAndForeignPropertySummaryPageSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  val cyaDiversionService: CYADiversionService = mock[CYADiversionService]
  val foreignCYADiversionService: ForeignCYADiversionService = mock[ForeignCYADiversionService]
  val mockUkSummaryPage: SummaryPage = mock[SummaryPage]
  val mockBusinessService: BusinessService = mock[BusinessService]

  def emptyUserAnswers: UserAnswers = UserAnswers("userAnswersId")

  val ukPropertyUserAnswers: Option[UserAnswers] = Some(
    emptyUserAnswers
      .set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      )
      .success
      .value
      .set(
        AboutPropertyCompletePage,
        true
      )
      .success
      .value
      .set(
        ReportPropertyIncomePage,
        true
      )
      .success
      .value
  )

  val foreignPropertyUserAnswers: Option[UserAnswers] = Some(
    emptyUserAnswers
      .set(
        ForeignSelectCountriesCompletePage,
        true
      )
      .success
      .value
  )

  val combinedUserAnswers: Option[UserAnswers] = Some(
    emptyUserAnswers
      .set(
        UKPropertyPage,
        Set[UKPropertySelect](UKPropertySelect.PropertyRentals)
      )
      .success
      .value
      .set(
        AboutPropertyCompletePage,
        true
      )
      .success
      .value
      .set(
        ReportPropertyIncomePage,
        true
      )
      .success
      .value
      .set(
        ForeignSelectCountriesCompletePage,
        true
      )
      .success
      .value
  )

  val noPropertyUserAnswers: Option[UserAnswers] = Some(
    emptyUserAnswers
  )

  private val taxYear = LocalDate.now.getYear

  def notStartPropertyAboutItems: Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "summary.about",
        controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
        TaskListTag.NotStarted,
        "property_about_link"
      )
    )

  def completedPropertyAboutItems: Seq[TaskListItem] =
    Seq(
      TaskListItem(
        "summary.about",
        controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
        TaskListTag.Completed,
        "property_about_link"
      )
    )

  def notStartedUkAndForeignPropertyItems: Seq[TaskListItem] = Seq(
    TaskListItem(
      "summary.aboutUKAndForeignProperties",
      controllers.ukandforeignproperty.routes.UkAndForeignPropertyDetailsController.onPageLoad(taxYear),
      TaskListTag.NotStarted,
      "uk_and_foreign_property_about_link"
    )
  )

  "UkAndForeignPropertySummaryPage" should {

    "return TaskListTag.NotStarted when both UK and Foreign properties are selected and completed" in {
      when(mockUkSummaryPage.propertyAboutItems(combinedUserAnswers, taxYear)).thenReturn(completedPropertyAboutItems)

      val foreignPropertyDetails =
        PropertyDetails(Some(ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      when(mockBusinessService.getForeignPropertyDetails(any(), any())(any()))
        .thenReturn(Future.successful(Right(Some(foreignPropertyDetails))))

      val result =
        UkAndForeignPropertySummaryPage.ukAndForeignPropertyAboutItems(
          taxYear,
          combinedUserAnswers,
          cyaDiversionService,
          foreignCYADiversionService
        )

      result shouldBe notStartedUkAndForeignPropertyItems
    }

    "return TaskListTag.CanNotStart when no properties are selected" in {
      val result = UkAndForeignPropertySummaryPage.ukAndForeignPropertyAboutItems(taxYear, None, cyaDiversionService, foreignCYADiversionService)

      result shouldBe notStartedUkAndForeignPropertyItems
    }

    "return Seq with TaskListTag.CanNotStart when only UK property is selected and completed" in {
      when(mockUkSummaryPage.propertyAboutItems(ukPropertyUserAnswers, taxYear)).thenReturn(completedPropertyAboutItems)

      val result =
        UkAndForeignPropertySummaryPage.ukAndForeignPropertyAboutItems(
          taxYear,
          ukPropertyUserAnswers,
          cyaDiversionService,
          foreignCYADiversionService
        )

      result should contain theSameElementsAs notStartedUkAndForeignPropertyItems
    }

    "return Seq with TaskListTag.CanNotStart when only foreign property is selected and completed" in {
      when(mockUkSummaryPage.propertyAboutItems(foreignPropertyUserAnswers, taxYear))
        .thenReturn(notStartPropertyAboutItems)

      val foreignPropertyDetails =
        PropertyDetails(Some(ForeignProperty.toString), Some(LocalDate.now), accrualsOrCash = Some(false), "incomeSourceId")

      when(mockBusinessService.getForeignPropertyDetails(any(), any())(any()))
        .thenReturn(Future.successful(Right(Some(foreignPropertyDetails))))

      val result =
        UkAndForeignPropertySummaryPage.ukAndForeignPropertyAboutItems(
          taxYear,
          foreignPropertyUserAnswers,
          cyaDiversionService,
          foreignCYADiversionService
        )

      result should contain theSameElementsAs notStartedUkAndForeignPropertyItems
    }

    "return Seq with TaskListTag.CanNotStart tag when no properties are completed" in {
      when(mockUkSummaryPage.propertyAboutItems(noPropertyUserAnswers, taxYear)).thenReturn(notStartPropertyAboutItems)

      when(mockBusinessService.getForeignPropertyDetails(any(), any())(any()))
        .thenReturn(Future.successful(Right(None)))

      val result =
        UkAndForeignPropertySummaryPage.ukAndForeignPropertyAboutItems(
          taxYear,
          noPropertyUserAnswers,
          cyaDiversionService,
          foreignCYADiversionService
        )

      result should contain theSameElementsAs notStartedUkAndForeignPropertyItems
    }
  }
}
