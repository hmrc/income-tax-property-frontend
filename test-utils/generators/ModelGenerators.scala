/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import models._
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryRarWhenYouReportedTheLoss: Arbitrary[WhenYouReportedTheLoss] =
    Arbitrary {
      Gen.oneOf(WhenYouReportedTheLoss.values.toSeq)
    }

  implicit lazy val arbitraryForeignWhenYouReportedTheLoss: Arbitrary[ForeignWhenYouReportedTheLoss] =
    Arbitrary {
      Gen.oneOf(ForeignWhenYouReportedTheLoss.values.toSeq)
    }

  implicit lazy val arbitraryReportIncome: Arbitrary[ReportIncome] =
    Arbitrary {
      Gen.oneOf(ReportIncome.values.toSeq)
    }

  implicit lazy val arbitraryUkAndForeignPropertyRentalTypeUk: Arbitrary[UkAndForeignPropertyRentalTypeUk] =
    Arbitrary {
      Gen.oneOf(UkAndForeignPropertyRentalTypeUk.values)
    }

  implicit lazy val arbitraryTotalPropertyIncome: Arbitrary[TotalPropertyIncome] =
    Arbitrary {
      Gen.oneOf(TotalPropertyIncome.values.toSeq)
    }

  implicit lazy val arbitraryUKProperty: Arbitrary[UKPropertySelect] =
    Arbitrary {
      Gen.oneOf(UKPropertySelect.values)
    }

  implicit lazy val arbitrarytotalIncome: Arbitrary[TotalIncome] =
    Arbitrary {
      Gen.oneOf(TotalIncome.values.toSeq)
    }
}
