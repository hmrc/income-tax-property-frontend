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

import pages.enhancedstructuresbuildingallowance.EsbaAddressPage
import pages.structurebuildingallowance.StructuredBuildingAllowanceAddressPage
import play.api.libs.json.Reads
import queries.Gettable

trait Addressable[T] {
  def getBuildingName(a: T): String

  def getPostcode(a: T): String

  def getBuildingNumber(a: T): String

  def get(index: Int, propertyType: PropertyType): Gettable[T]

}

object Addressable {

  private def standardise(info: String): String = info.trim.toLowerCase().filterNot(_.isSpaceChar)

  def checkAddresses[T, U](first: T, other: U)(implicit
    addressableFirst: Addressable[T],
    addressableSecond: Addressable[U]
  ): Boolean =
    standardise(addressableFirst.getPostcode(first)).equals(standardise(addressableSecond.getPostcode(other))) &&
      standardise(addressableFirst.getBuildingName(first))
        .equals(standardise(addressableSecond.getBuildingName(other))) &&
      standardise(addressableFirst.getBuildingNumber(first))
        .equals(standardise(addressableSecond.getBuildingNumber(other)))

  def get[A](index: Int, propertyType: PropertyType)(implicit a: Addressable[A]): Gettable[A] =
    a.get(index, propertyType)

  def getAddresses[T](
    userAnswers: UserAnswers,
    propertyType: PropertyType,
    indexToExclude: Option[Int]
  )(implicit
    a: Addressable[T],
    r: Reads[T]
  ): List[T] =
    Iterator
      .from(0)
      .map(index => userAnswers.get(get[T](index, propertyType)).map((index, _)))
      .takeWhile(_.isDefined)
      .collect {
        case Some((index, address)) if !indexToExclude.contains(index) => address
      }
      .toList

  implicit val sbaAddressable: Addressable[StructuredBuildingAllowanceAddress] =
    new Addressable[StructuredBuildingAllowanceAddress] {
      override def getBuildingName(address: StructuredBuildingAllowanceAddress): String = address.buildingName

      override def getPostcode(address: StructuredBuildingAllowanceAddress): String = address.postCode

      override def getBuildingNumber(address: StructuredBuildingAllowanceAddress): String = address.buildingNumber

      override def get(index: Int, propertyType: PropertyType): Gettable[StructuredBuildingAllowanceAddress] =
        StructuredBuildingAllowanceAddressPage(index, propertyType)
    }

  implicit val esbaAddressable: Addressable[EsbaAddress] = new Addressable[EsbaAddress] {
    override def getBuildingName(address: EsbaAddress): String = address.buildingName

    override def getPostcode(address: EsbaAddress): String = address.postCode

    override def getBuildingNumber(address: EsbaAddress): String = address.buildingNumber

    override def get(index: Int, propertyType: PropertyType): Gettable[EsbaAddress] =
      EsbaAddressPage(index, propertyType)
  }
}
