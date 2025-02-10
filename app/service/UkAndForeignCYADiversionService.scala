/*
 * Copyright 2025 HM Revenue & Customs
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

import models.{Mode, NormalMode, UserAnswers}
import pages.QuestionPage
import pages.ukandforeignproperty.SectionCompletePage
import play.api.mvc.Call
import queries.Gettable

import javax.inject.Inject

class UkAndForeignCYADiversionService @Inject() {
  def about[T](
    taxYear: Int,
    userAnswers: UserAnswers
  )(
    block: => T
  )(transform: Call => T): PartialFunction[(Mode, String), T] = {
    case (NormalMode, UkAndForeignCYADiversionService.ABOUT) =>
      divert(SectionCompletePage, userAnswers, block)(
        cyaDiversion = controllers.ukandforeignproperty.routes.UkAndForeignPropertyCheckYourAnswersController.onPageLoad(taxYear)
      )(transform)
  }

  def redirectCallToCYAIfFinished(
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    journeyName: String
  )(
    block: => Call
  ): Call =
    redirectToCYAIfFinished(taxYear, userAnswers, journeyName, NormalMode)(block)(identity[Call])
  def forOther[T](block: => T): PartialFunction[(Mode, String), T] = { case _ =>
    block
  }

  def redirectToCYAIfFinished[T](
    taxYear: Int,
    userAnswers: Option[UserAnswers],
    journeyName: String,
    mode: Mode
  )(
    block: => T
  )(transform: Call => T): T =
    userAnswers.fold(
      forOther(block)(mode, journeyName)
    )(ua => redirectToCYAIfFinished(taxYear, ua, journeyName, mode)(block)(transform))

  def redirectToCYAIfFinished[T](
    taxYear: Int,
    userAnswers: UserAnswers,
    journeyName: String,
    mode: Mode
  )(
    block: => T
  )(transform: Call => T): T =
    about(taxYear, userAnswers)(block)(transform)
      .orElse(forOther(block))((mode, journeyName))

  private def divert[T](questionPage: QuestionPage[Boolean], userAnswers: UserAnswers, block: => T)(
    cyaDiversion: Call
  )(transform: Call => T): T =
    if (isJourneyFinished(userAnswers, questionPage)) {
      transform(cyaDiversion)
    } else {
      block
    }

  private def isJourneyFinished(userAnswers: UserAnswers, page: Gettable[Boolean]): Boolean =
    userAnswers.get(page).getOrElse(false)
}

object UkAndForeignCYADiversionService {
  val ABOUT = "about"
}
