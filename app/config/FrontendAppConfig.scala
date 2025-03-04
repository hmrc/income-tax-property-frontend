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

package config

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@ImplementedBy(classOf[FrontendAppConfigImpl])
trait FrontendAppConfig {
  def host: String
  def appName: String
  def contactHost: String
  def contactFormServiceIdentifier: String
  def feedbackUrl(implicit request: RequestHeader): String
  def loginUrl: String
  def loginContinueUrl: String
  def signOutUrl: String
  def exitSurveyBaseUrl: String
  def exitSurveyUrl: String
  def incomeTaxSubmissionBaseUrl: String
  def incomeTaxSubmissionIvRedirect: String
  def propertyServiceBaseUrl: String
  def languageTranslationEnabled: Boolean
  def languageMap: Map[String, Lang]
  def timeout: Int
  def countdown: Int
  def cacheTtl: Int
  def cacheTtlSecondsOrDays: String
  def viewAndChangeEnterUtrUrl: String
  def viewAndChangeViewUrlAgent: String
}

@Singleton
class FrontendAppConfigImpl @Inject() (configuration: Configuration) extends FrontendAppConfig {

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  val contactHost = configuration.get[String]("contact-frontend.host")
  val contactFormServiceIdentifier = "income-tax-property-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = configuration.get[String]("urls.signOut")

  val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/income-tax-property-frontend"

  private lazy val incomeTaxSubmissionFrontendUrlKey = "microservice.services.income-tax-submission-frontend.url"
  def incomeTaxSubmissionBaseUrl: String = configuration.get[String](incomeTaxSubmissionFrontendUrlKey) +
    configuration.get[String]("microservice.services.income-tax-submission-frontend.context")

  def incomeTaxSubmissionIvRedirect: String = incomeTaxSubmissionBaseUrl +
    configuration.get[String]("microservice.services.income-tax-submission-frontend.iv-redirect")

  private lazy val propertyUrlKey = "microservice.services.income-tax-property.url"
  lazy val propertyServiceBaseUrl: String = s"${configuration.get[String](propertyUrlKey)}/income-tax-property"

  val languageTranslationEnabled: Boolean = configuration.get[Boolean]("feature-switch.welshToggleEnabled")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLive")
  val cacheTtlSecondsOrDays: String = configuration.get[String]("mongodb.timeToLiveDaysOrSeconds")

  val vcBaseUrl = configuration.get[String]("microservice.services.view-and-change.url")

  val viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"
  def viewAndChangeViewUrlAgent: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents"

}
