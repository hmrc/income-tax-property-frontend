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

package controllers.actions

import base.SpecBase
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.MockAppConfig
import connectors.MockAuthConnector
import handlers.MockErrorHandler
import models.authorisation
import models.authorisation.{DelegatedAuthRules, SessionValues}
import models.errors.MissingAgentClientDetails
import models.session.UserSessionData
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, BodyParsers, MessagesControllerComponents, Request, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.SessionDataService
import mocks.MockSessionDataService
import testHelpers.UserHelper.aUser
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.~
import utils.EnrolmentHelper.{agentAuthPredicate, secondaryAgentPredicate}

import scala.concurrent.Future

class AuthActionSpec extends SpecBase with MockAppConfig with MockAuthConnector with MockSessionDataService with MockErrorHandler {

  val mtditid = "123456789"
  val nino = "AA123456C"
  val arn = "012345678"

  trait Fixture {

    MockAppConfig.loginUrl("/sign-in")
    MockAppConfig.viewAndChangeEnterUtrUrl("/enter-utr")
    MockAppConfig.incomeTaxSubmissionIvRedirect("/iv-uplift")

    lazy implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    lazy val application = applicationBuilder(userAnswers = None, isAgent = false).build()
    lazy val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
    lazy val mcc: MessagesControllerComponents = stubMessagesControllerComponents()

    val sessionData: UserSessionData = UserSessionData(aUser.sessionId, mtditid, nino)

    val request: Request[AnyContent] = FakeRequest().withSession(SessionValues.SessionId -> aUser.sessionId)

    lazy val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, mockErrorHandler, mockSessionDataService, bodyParsers)(mockAppConfig, mcc)

    class Harness(authAction: IdentifierAction) {
      def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
    }

    lazy val controller = new Harness(authAction)
  }

  "Auth Action" - {

    "when the user is an Individual" - {

      "authorised with a satisfactory confidence level" - {

        "has a NINO and MTDITID enrolment" - {

          "must return OK" in new Fixture {

            val enrolments = Enrolments(
              Set(
                Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(Some("internalId"), Some(AffinityGroup.Individual)))
              )
              .once()

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(enrolments, ConfidenceLevel.L250))
              )
              .once()

            val result = controller.onPageLoad()(request)

            status(result) mustBe OK
          }
        }

        "has a missing NINO" - {

          "must return SEE_OTHER and redirect to login" in new Fixture {

            val enrolments = Enrolments(
              Set(
                Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(Some("internalId"), Some(AffinityGroup.Individual)))
              )
              .once()

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(enrolments, ConfidenceLevel.L250))
              )
              .once()

            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(mockAppConfig.loginUrl)
          }
        }

        "has a missing MTDITID" - {

          "must return SEE_OTHER and redirect to login" in new Fixture {

            val enrolments = Enrolments(
              Set(
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(Some("internalId"), Some(AffinityGroup.Individual)))
              )
              .once()

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(enrolments, ConfidenceLevel.L250))
              )
              .once()

            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(mockAppConfig.loginUrl)
          }
        }

        "has a missing SessionId" - {

          "must return SEE_OTHER and redirect to login" in new Fixture {
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(mockAppConfig.loginUrl)
          }
        }
      }

      "unauthorised with an insufficient confidence level" - {

        "must return SEE_OTHER and redirect to IV Uplift journey" in new Fixture {

          val enrolments = Enrolments(
            Set(
              Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
              Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated")
            ))

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new~(Some("internalId"), Some(AffinityGroup.Individual)))
            )
            .once()

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new~(enrolments, ConfidenceLevel.L200))
            )
            .once()

          val result = controller.onPageLoad()(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(mockAppConfig.incomeTaxSubmissionIvRedirect)
        }
      }
    }

    "when user is an Agent" - {

      "when a clientID and NINO are in session" - {

        val fakeRequestWithMtditidAndNINO = FakeRequest().withSession(
          SessionValues.ClientMtdid -> mtditid,
          SessionValues.ClientNino  -> nino,
          SessionValues.SessionId -> aUser.sessionId
        )

        "Authorised as a Primary Agent" - {

          "must return OK" in new Fixture {

            mockGetSessionData(aUser.sessionId)(sessionData)

            val enrolments = Enrolments(
              Set(
                Enrolment(
                  key = EnrolmentKeys.Individual,
                  identifiers = Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)),
                  state = "Activated",
                  delegatedAuthRule = Some(DelegatedAuthRules.agentDelegatedAuthRule)
                ),
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated"),
                Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, arn)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent))))

            MockAuthConnector
              .authorise(agentAuthPredicate(mtditid))(Future.successful(enrolments))

            val result = controller.onPageLoad()(fakeRequestWithMtditidAndNINO)

            status(result) mustBe OK
          }
        }

        "Not Authorised as a Primary Agent" - {

          "when a Secondary Agent attempts to login with valid credentials" in new Fixture {

            mockGetSessionData(aUser.sessionId)(sessionData)

            val enrolments = Enrolments(Set(
              Enrolment(
                key = EnrolmentKeys.Supporting,
                identifiers = Seq(EnrolmentIdentifier(EnrolmentIdentifiers.supportingAgentId, mtditid)),
                state = "Activated",
                delegatedAuthRule = Some(DelegatedAuthRules.supportingAgentDelegatedAuthRule)
              ),
              Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated"),
              Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, arn)), "Activated")
            ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new~(Some("internalId"), Some(AffinityGroup.Agent)))
              )

            MockAuthConnector
              .authorise(agentAuthPredicate(mtditid))(Future.failed(InsufficientEnrolments()))

            MockAuthConnector
              .authorise(secondaryAgentPredicate(mtditid))(Future.successful(enrolments))

            val result = controller.onPageLoad()(fakeRequestWithMtditidAndNINO)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.routes.SupportingAgentAuthErrorController.show.url)
          }

          "when NOT authorised as a Secondary Agent" - {

            "when the Exception is any other type of Unexpected Exception" - {

              "must render ISE (500)" in new Fixture {

                mockGetSessionData(aUser.sessionId)(sessionData)

                MockAuthConnector
                  .authorise(EmptyPredicate)(
                    Future.successful(new~(Some("internalId"), Some(AffinityGroup.Agent)))
                  )

                MockAuthConnector
                  .authorise(agentAuthPredicate(mtditid))(Future.failed(InsufficientEnrolments()))

                MockAuthConnector
                  .authorise(secondaryAgentPredicate(mtditid))(Future.failed(new Exception("bang")))

                mockInternalServerError()

                val result = controller.onPageLoad()(fakeRequestWithMtditidAndNINO)

                status(result) mustBe INTERNAL_SERVER_ERROR
                contentAsString(result) mustBe "There is a problem."
              }
            }
          }

          "when the Exception is any other type of Unexpected Exception" - {

            "must render ISE (500)" in new Fixture {

              mockGetSessionData(aUser.sessionId)(sessionData)

              MockAuthConnector
                .authorise(EmptyPredicate)(
                  Future.successful(new~(Some("internalId"), Some(AffinityGroup.Agent)))
                )

              MockAuthConnector
                .authorise(agentAuthPredicate(mtditid))(Future.failed(new Exception("bang")))

              mockInternalServerError()

              val result = controller.onPageLoad()(fakeRequestWithMtditidAndNINO)

              status(result) mustBe INTERNAL_SERVER_ERROR
              contentAsString(result) mustBe "There is a problem."
            }
          }
        }
      }

      "when a clientID is missing from session" - {

        val fakeRequestWithNINO = FakeRequest().withSession(
          SessionValues.ClientNino -> nino,
          SessionValues.SessionId -> aUser.sessionId
        )

        "must return SEE_OTHER (303) and redirect to Unauthorised page" in new Fixture {
          mockGetSessionDataException(aUser.sessionId)(MissingAgentClientDetails("No session data"))

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent)))
            )

          val result = controller.onPageLoad()(fakeRequestWithNINO)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(mockAppConfig.viewAndChangeEnterUtrUrl)
        }
      }

      "when a NINO is missing from session" - {

        val fakeRequestWithMtditid = FakeRequest().withSession(
          SessionValues.ClientMtdid -> mtditid,
          SessionValues.SessionId -> aUser.sessionId
        )

        "must return SEE_OTHER (303) and redirect to Agent Error page" in new Fixture {
          mockGetSessionDataException(aUser.sessionId)(MissingAgentClientDetails("No session data"))

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent)))
            )

          val result = controller.onPageLoad()(fakeRequestWithMtditid)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(mockAppConfig.viewAndChangeEnterUtrUrl)
        }
      }

      "when a SessionId is missing from session" - {
        val fakeRequestWithMtditid = FakeRequest().withSession(
          SessionValues.ClientMtdid -> mtditid,
          SessionValues.ClientNino -> nino,
        )

        "must return SEE_OTHER (303) and redirect to Sign In page" in new Fixture {
          val result = controller.onPageLoad()(fakeRequestWithMtditid)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(mockAppConfig.loginUrl)
        }
      }
    }

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(MissingBearerToken()))

        val result = controller.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(mockAppConfig.loginUrl)
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(BearerTokenExpired()))

        val result = controller.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(mockAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(InsufficientEnrolments()))

        val result = controller.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(UnsupportedAuthProvider()))

        val result = controller.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(UnsupportedAffinityGroup()))

        val result = controller.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(UnsupportedCredentialRole()))

        val result = controller.onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "any other type of unexpected exception has bubbled up" - {

      "render ISE" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(new Exception("bang")))
        mockInternalServerError()

        val result = controller.onPageLoad()(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe "There is a problem."
      }
    }
  }
}
