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

package views
import base.SpecBase
import play.api.test.Helpers._
import play.api.test._
import play.api.mvc._
import play.twirl.api.Html
import views.html.SummaryView
import controllers.routes
import org.scalatest.matchers.must.Matchers
import pages.UKPropertySummaryPage
import pages.foreign.ForeignPropertySummaryPage
import pages.ukandforeignproperty.UkAndForeignPropertySummaryPage
import play.api.Application
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.govukfrontend.views.html.components.GovukTag
import viewmodels.summary.{TaskListItem, TaskListTag}
import views.html.templates.Layout

import java.time.LocalDate

class SummaryViewSpec extends SpecBase with Matchers {

  val application: Application = new GuiceApplicationBuilder().build()

  implicit val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  val layout: Layout     = application.injector.instanceOf[Layout]
  val govukTag: GovukTag = application.injector.instanceOf[GovukTag]
  val view: SummaryView  = application.injector.instanceOf[SummaryView]
  private val taxYear    = LocalDate.now.getYear


  def createView(
                  ukProperty: UKPropertySummaryPage,
                  foreignProperty: ForeignPropertySummaryPage,
                  ukAndForeignProperty: UkAndForeignPropertySummaryPage
                )(implicit request: Request[_]): Html = {
    view(ukProperty, foreignProperty, ukAndForeignProperty)(request, messages)
  }

  "Summary view" - {

    "render the view with completed UK property items" in {
      val ukProperty = UKPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.about",
            controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.Completed,
            "property_about_link"
          )
        ),
        rentalsRows = Seq.empty,
        rentARoomRows = Seq.empty,
        combinedItems = Seq.empty
      )

      val foreignProperty = ForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq.empty,
        foreignPropertyItems = Map.empty,
        foreignIncomeCountries = List.empty,
        userAnswers = None
      )

      val ukAndForeignProperty = UkAndForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq.empty
      )

      val request = FakeRequest(GET, routes.SummaryController.show(taxYear).url)
      val html = createView(ukProperty, foreignProperty, ukAndForeignProperty)(request)

      contentAsString(html) must include(messages("summary.aboutUKProperties.heading"))
      contentAsString(html) must include(messages("summary.about"))
      contentAsString(html) must include(messages("common.completed"))
      contentAsString(html) must not include messages("ukAndForeign.summary.title")
    }

    "render the view with completed foreign property items" in {
      val ukProperty = UKPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq.empty,
        rentalsRows = Seq.empty,
        rentARoomRows = Seq.empty,
        combinedItems = Seq.empty
      )

      val foreignProperty = ForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "foreign.selectCountry",
            controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.Completed,
            "foreign_property_select_country"
          )
        ),
        foreignPropertyItems = Map.empty,
        foreignIncomeCountries = List.empty,
        userAnswers = None
      )

      val ukAndForeignProperty = UkAndForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq.empty
      )

      val request = FakeRequest(GET, routes.SummaryController.show(taxYear).url)
      val html = createView(ukProperty, foreignProperty, ukAndForeignProperty)(request)

      contentAsString(html) must include(messages("foreign.summary.title"))
      contentAsString(html) must include(messages("foreign.selectCountry"))
      contentAsString(html) must include(messages("common.completed"))
      contentAsString(html) must not include messages("summary.aboutUKAndForeignProperties")
      contentAsString(html) must not include messages("ukAndForeign.summary.title")
    }

    "render the view with both UK and foreign property items" in {
      val ukProperty = UKPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.about",
            controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.Completed,
            "property_about_link"
          )
        ),
        rentalsRows = Seq.empty,
        rentARoomRows = Seq.empty,
        combinedItems = Seq.empty
      )

      val foreignProperty = ForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "foreign.selectCountry",
            controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.Completed,
            "foreign_property_select_country"
          )
        ),
        foreignIncomeCountries = List.empty,
        foreignPropertyItems = Map.empty,
        userAnswers = None
      )

      val ukAndForeignProperty = UkAndForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.aboutUKAndForeignProperties",
            routes.SummaryController.show(taxYear),
            TaskListTag.NotStarted,
            "uk_and_foreign_property_about_link"
          )
        )
      )

      val request = FakeRequest(GET, routes.SummaryController.show(taxYear).url)
      val html = createView(ukProperty, foreignProperty, ukAndForeignProperty)(request)

      contentAsString(html) must include(messages("summary.aboutUKProperties.heading"))
      contentAsString(html) must include(messages("summary.about"))
      contentAsString(html) must include(messages("foreign.summary.title"))
      contentAsString(html) must include(messages("foreign.selectCountry"))
      contentAsString(html) must include(messages("ukAndForeign.summary.title"))
      contentAsString(html) must include(messages("summary.aboutUKAndForeignProperties"))
      contentAsString(html) must include(messages("common.completed"))

      contentAsString(html) must include("UK property")
      contentAsString(html) must include("UK and Foreign property")
      contentAsString(html) must include("Select country")
      contentAsString(html) must include("UK and Foreign property")
      contentAsString(html) must include("About")

      contentAsString(html) must include("Completed")
      contentAsString(html) must include("Not yet started")
    }

    "render the view with both UK and foreign property items as Cannot start yet because foreign property is not completed" in {
      val ukProperty = UKPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.about",
            controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.Completed,
            "property_about_link"
          )
        ),
        rentalsRows = Seq.empty,
        rentARoomRows = Seq.empty,
        combinedItems = Seq.empty
      )

      val foreignProperty = ForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "foreign.selectCountry",
            controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "foreign_property_select_country"
          )
        ),
        foreignIncomeCountries = List.empty,
        foreignPropertyItems = Map.empty,
        userAnswers = None
      )

      val ukAndForeignProperty = UkAndForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.aboutUKAndForeignProperties",
            routes.SummaryController.show(taxYear),
            TaskListTag.CanNotStart,
            "uk_and_foreign_property_about_link"
          )
        )
      )

      val request = FakeRequest(GET, routes.SummaryController.show(taxYear).url)
      val html = createView(ukProperty, foreignProperty, ukAndForeignProperty)(request)

      contentAsString(html) must include("Cannot start yet")
    }

    "render the view with both UK and foreign property items as Cannot start yet because UK property is not completed" in {
      val ukProperty = UKPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.about",
            controllers.about.routes.UKPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.NotStarted,
            "property_about_link"
          )
        ),
        rentalsRows = Seq.empty,
        rentARoomRows = Seq.empty,
        combinedItems = Seq.empty
      )

      val foreignProperty = ForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "foreign.selectCountry",
            controllers.foreign.routes.ForeignPropertyDetailsController.onPageLoad(taxYear),
            TaskListTag.Completed,
            "foreign_property_select_country"
          )
        ),
        foreignIncomeCountries = List.empty,
        foreignPropertyItems = Map.empty,
        userAnswers = None
      )

      val ukAndForeignProperty = UkAndForeignPropertySummaryPage(
        taxYear = taxYear,
        startItems = Seq(
          TaskListItem(
            "summary.aboutUKAndForeignProperties",
            routes.SummaryController.show(taxYear),
            TaskListTag.CanNotStart,
            "uk_and_foreign_property_about_link"
          )
        )
      )

      val request = FakeRequest(GET, routes.SummaryController.show(taxYear).url)
      val html = createView(ukProperty, foreignProperty, ukAndForeignProperty)(request)

      contentAsString(html) must include("Cannot start yet")
    }
  }
}