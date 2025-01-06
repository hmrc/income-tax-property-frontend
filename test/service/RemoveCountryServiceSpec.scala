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

import base.SpecBase
import models.requests.DataRequest
import models.{Index, User, UserAnswers}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.foreign.Country
import pages.ukandforeignproperty.SelectCountryPage
import play.api.mvc.AnyContent
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RemoveCountryServiceSpec extends SpecBase with FutureAwaits with DefaultAwaitTimeout {

  val france: Country = Country("France", "FR")
  val spain: Country = Country("Spain", "ES")
  val testCountries: List[Country] = List(france, spain)
  val user: User = User("mtditid", "nino", "group", None)

  private def buildUserAnswers(countries: List[Country]): UserAnswers =
    emptyUserAnswers.set(SelectCountryPage, countries).success.value

  private def buildDataRequest(countries: List[Country]): DataRequest[AnyContent] =
    DataRequest(FakeRequest(), userAnswersId, user, buildUserAnswers(countries))

  private def mockSessionSet(mockSessionRepository: SessionRepository, countries: List[Country]): OngoingStubbing[Future[Boolean]] =
    when(
      mockSessionRepository.set(
        argThat {
          answers: UserAnswers =>
            (answers.data \ "countries").as[List[Country]] == countries
        }
      )
    ).thenReturn(Future.successful(true))

  "remove country" - {

    "should remove the 1st country from list when called with Index(1)" in {
      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      val service = new RemoveCountryService(mockSessionRepository)

      mockSessionSet(mockSessionRepository, List(spain))

      val result    = await(service.removeCountry(Index(1))(buildDataRequest(testCountries)))
      val countries = result.get(SelectCountryPage)
      countries mustBe Some(List(spain))
    }

    "should remove the 2nd country from list when called with Index(2)" in {
      val mockSessionRepository: SessionRepository = mock[SessionRepository]

      val service = new RemoveCountryService(mockSessionRepository)
      mockSessionSet(mockSessionRepository, List(france))

      val result    = await(service.removeCountry(Index(2))(buildDataRequest(testCountries)))
      val countries = result.get(SelectCountryPage)
      countries mustBe Some(List(france))
    }

    "should throw an IndexOutOfBoundsException when called with Index(3)" in {
      val mockSessionRepository: SessionRepository = mock[SessionRepository]
      val service = new RemoveCountryService(mockSessionRepository)
      val result = service.removeCountry(Index(3))(buildDataRequest(testCountries))

      result.failed.futureValue mustBe an[IndexOutOfBoundsException]
    }
  }
}
