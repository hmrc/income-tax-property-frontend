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

package testHelpers

import models._

trait Fixture {
  val ukPropertyData: FetchedUKPropertyData = FetchedUKPropertyData(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    List(),
    None
  )
  val foreignPropertyData: FetchedForeignPropertyData = FetchedForeignPropertyData(None,None,None,None, None,None,None)
  val ukAndForeignPropertyData: FetchedUkAndForeignPropertyData = FetchedUkAndForeignPropertyData(None)
  val fetchedPropertyData: FetchedPropertyData = FetchedPropertyData(Some(ukPropertyData), Some(foreignPropertyData), Some(ukAndForeignPropertyData))
  val fetchedData: FetchedData = FetchedData(propertyData = fetchedPropertyData, incomeData = None)
}
