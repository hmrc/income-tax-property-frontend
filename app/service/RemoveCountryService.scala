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

package service

import jakarta.inject.Inject
import models.{Index, UserAnswers}
import models.requests.DataRequest
import pages.ukandforeignproperty.SelectCountryPage
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class RemoveCountryService @Inject()(sessionRepository: SessionRepository)
                                    (implicit ec: ExecutionContext) {

  def removeCountry(index: Index)(implicit request: DataRequest[_]): Future[UserAnswers] =
    for {
      countries               <- Future { request.userAnswers.get(SelectCountryPage).getOrElse(Nil) }
      countryToRemove         = countries.lift(index.positionZeroIndexed)
                                  .getOrElse(throw new IndexOutOfBoundsException(s"No country exists for position index: ${index.position}"))
      updatedCountries        = countries.filterNot(_ == countryToRemove)
      updatedUserAnswers      <- Future.fromTry(request.userAnswers.set(SelectCountryPage, updatedCountries))
      _                       <- sessionRepository.set(updatedUserAnswers)
    } yield updatedUserAnswers

}
