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

package config

import org.scalamock.handlers.CallHandler0
import org.scalamock.scalatest.MockFactory

trait MockAppConfig extends MockFactory {

  lazy val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  object MockAppConfig {

    def loginUrl(url: String): CallHandler0[String] =
      (() => mockAppConfig.loginUrl).expects().returns(url).anyNumberOfTimes()

    def viewAndChangeEnterUtrUrl(url: String): CallHandler0[String] =
      (() => mockAppConfig.viewAndChangeEnterUtrUrl).expects().returns(url).anyNumberOfTimes()

    def incomeTaxSubmissionIvRedirect(url: String): CallHandler0[String] =
      (() => mockAppConfig.incomeTaxSubmissionIvRedirect).expects().returns(url).anyNumberOfTimes()
  }

}
