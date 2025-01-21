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

package models

import pages.foreign.structurebuildingallowance.ForeignStructuresBuildingAllowanceAddressPage
import play.api.libs.json.Reads
import queries.Gettable

trait ForeignAddressable[T] {
  def getBuildingName(a: T): String

  def getPostcode(a: T): String

  def getBuildingNumber(a: T): String

  def get(index: Int, countryCode: String): Gettable[T]

}

object ForeignAddressable {

  private def standardise(info: String): String = info.trim.toLowerCase().filterNot(_.isSpaceChar)

  def checkForeignAddresses[T, U](first: T, other: U)(implicit
    addressableFirst: ForeignAddressable[T],
    addressableSecond: ForeignAddressable[U]
  ): Boolean =
    standardise(addressableFirst.getPostcode(first)).equals(standardise(addressableSecond.getPostcode(other))) &&
      standardise(addressableFirst.getBuildingName(first))
        .equals(standardise(addressableSecond.getBuildingName(other))) &&
      standardise(addressableFirst.getBuildingNumber(first))
        .equals(standardise(addressableSecond.getBuildingNumber(other)))

  def get[A](index: Int, countryCode: String)(implicit a: ForeignAddressable[A]): Gettable[A] =
    a.get(index, countryCode)

  def getForeignAddresses[T](
    userAnswers: UserAnswers,
    countryCode: String,
    indexToExclude: Option[Int]
  )(implicit
    a: ForeignAddressable[T],
    r: Reads[T]
  ): List[T] =
    Iterator
      .from(0)
      .map(index => userAnswers.get(get[T](index, countryCode)).map((index, _)))
      .takeWhile(_.isDefined)
      .collect {
        case Some((index, address)) if !indexToExclude.contains(index) => address
      }
      .toList

  implicit val foreignSbaAddressable: ForeignAddressable[ForeignStructuresBuildingAllowanceAddress] =
    new ForeignAddressable[ForeignStructuresBuildingAllowanceAddress] {
      override def getBuildingName(address: ForeignStructuresBuildingAllowanceAddress): String = address.name

      override def getPostcode(address: ForeignStructuresBuildingAllowanceAddress): String = address.postCode

      override def getBuildingNumber(address: ForeignStructuresBuildingAllowanceAddress): String =
        address.number

      override def get(index: Int, countryCode: String): Gettable[ForeignStructuresBuildingAllowanceAddress] =
        ForeignStructuresBuildingAllowanceAddressPage(index, countryCode)
    }
}
