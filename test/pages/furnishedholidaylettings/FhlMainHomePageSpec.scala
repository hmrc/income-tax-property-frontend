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

package pages.furnishedholidaylettings

import base.SpecBase
import models.FhlClaimPiaOrExpenses.Pia
import models.FhlReliefOrExpenses.Rentaroomrelief
import models.UserAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks._

import scala.util.Success

class FhlMainHomePageSpec extends SpecBase {

  private val userAnswerWithJointlyLetAndReliefOrExpenses: UserAnswers = (for {
    ua <- emptyUserAnswers.set(FhlJointlyLetPage, true)
    updatedUserAnswers <- ua.set(FhlReliefOrExpensesPage, Rentaroomrelief)
  } yield updatedUserAnswers).get

  private val userAnswerWithPiaOrExpenses: UserAnswers = emptyUserAnswers.set(FhlClaimPiaOrExpensesPage, Pia).get

  private val scenarios = Table[Boolean, UserAnswers, String, UserAnswers](
    ("mainHome", "userAnswers", "description", "userAnswersAfterCleanup"),
    (true, userAnswerWithJointlyLetAndReliefOrExpenses, "having JointlyLet and ReliefOrExpenses", userAnswerWithJointlyLetAndReliefOrExpenses),
    (false, userAnswerWithJointlyLetAndReliefOrExpenses, "having JointlyLet and ReliefOrExpenses", emptyUserAnswers),
    (true, userAnswerWithPiaOrExpenses, "having PiaOrExpenses", emptyUserAnswers),
    (false, userAnswerWithPiaOrExpenses, "having PiaOrExpenses", userAnswerWithPiaOrExpenses)
  )

  "FhlMainHomePage " - {
    forAll(scenarios) { (mainHomeAnswer: Boolean, userAnswers: UserAnswers, description: String, userAnswersAfterCleanup: UserAnswers) => {
      s"$description with mainHome $mainHomeAnswer $description" in {
        FhlMainHomePage.cleanup(Some(mainHomeAnswer), userAnswers).map(_.data) mustEqual (Success(userAnswersAfterCleanup.data))
      }
    }
    }
  }
}
