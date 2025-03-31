package controllers.foreignincome.dividends

import base.SpecBase
import controllers.routes
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.ForeignDividendsStartView

class ForeignDividendsStartControllerSpec extends SpecBase {

  "ForeignDividendsStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ForeignDividendsStartController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ForeignDividendsStartView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
