/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import java.time.LocalDate

import base.SpecBase
import connectors.TrustStoreConnector
import forms.{AddAnOtherIndividualFormProvider, YesNoFormProvider}
import models.{AddAnOtherIndividual, Name, NationalInsuranceNumber, NormalMode, OtherIndividual, OtherIndividuals, RemoveOtherIndividual}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddAnOtherIndividualViewHelper
import viewmodels.addAnother.AddRow
import views.html.{AddAnOtherIndividualView, AddAnOtherIndividualYesNoView, MaxedOutOtherIndividualsView}

import scala.concurrent.{ExecutionContext, Future}

class AddAnOtherIndividualControllerSpec extends SpecBase with ScalaFutures {

  lazy val getRoute : String = controllers.routes.AddAnOtherIndividualController.onPageLoad().url
  lazy val submitOneRoute : String = controllers.routes.AddAnOtherIndividualController.submitOne().url
  lazy val submitAnotherRoute : String = controllers.routes.AddAnOtherIndividualController.submitAnother().url
  lazy val submitCompleteRoute : String = controllers.routes.AddAnOtherIndividualController.submitComplete().url

  val mockStoreConnector : TrustStoreConnector = mock[TrustStoreConnector]

  val addOtherIndividualForm: Form[AddAnOtherIndividual] = new AddAnOtherIndividualFormProvider()()
  val addOtherIndividualYesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addAnOtherIndividualYesNo")

  private val otherIndividual = OtherIndividual(
    name = Name(firstName = "First", middleName = None, lastName = "Last"),
    dateOfBirth = Some(LocalDate.parse("1983-09-24")),
    identification = Some(NationalInsuranceNumber("JS123456A")),
    address = None,
    entityStart = LocalDate.parse("2019-02-28"),
    provisional = true
  )

  private val otherIndividuals = OtherIndividuals(List(otherIndividual))

  lazy val featureNotAvailable : String = controllers.routes.FeatureNotAvailableController.onPageLoad().url

  val otherIndividualRows = List(
    AddRow("First Last", typeLabel = "Other individual", "Change details", Some(featureNotAvailable), "Remove", Some(controllers.individual.remove.routes.RemoveOtherIndividualController.onPageLoad(0).url))
  )

  class FakeService(data: OtherIndividuals) extends TrustService {
    override def getOtherIndividuals(utr: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OtherIndividuals] = Future.successful(data)

    override def getOtherIndividual(utr: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[OtherIndividual] =
      Future.successful(otherIndividual)

    override def removeOtherIndividual(utr: String, otherIndividual: RemoveOtherIndividual)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(OK))
  }

  "AddAnOtherIndividual Controller" when {

    "no other individuals" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = None).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAnOtherIndividual.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAnOtherIndividualYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addOtherIndividualYesNoForm
          )(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user answers no" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request =
          FakeRequest(POST, submitOneRoute)
            .withFormUrlEncodedBody(("value", "false"))

        when(mockStoreConnector.setTaskComplete(any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to feature unavailable when the user answers yes" in {

        val fakeService = new FakeService(OtherIndividuals(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
          )).build()

        val request =
          FakeRequest(POST, submitOneRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual featureNotAvailable

        application.stop()
      }

    }

    "there are other individuals" must {

      "return OK and the correct view for a GET" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAnOtherIndividualView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            addOtherIndividualForm,
            Nil,
            otherIndividualRows,
            "Add another individual"
          )(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAnOtherIndividual.NoComplete.toString))

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to the maintain task list when the user says they want to add later" ignore {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAnOtherIndividual.YesLater.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()
      }

      "redirect to feature unavailable when the user answers yes now" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
          )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", AddAnOtherIndividual.YesNow.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.NameController.onPageLoad(NormalMode).url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val otherIndividuals = OtherIndividuals(List.fill(2)(otherIndividual))
        val fakeService = new FakeService(otherIndividuals)
        val otherIndividualRows = new AddAnOtherIndividualViewHelper(otherIndividuals.otherIndividuals).rows

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request =
          FakeRequest(POST, submitAnotherRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addOtherIndividualForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAnOtherIndividualView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            otherIndividualRows.inProgress,
            otherIndividualRows.complete,
            "The trust has 2 individuals"
          )(fakeRequest, messages).toString

        application.stop()
      }
    }

    "maxed out other individuals" must {

      "return OK and the correct view for a GET" in {

        val otherIndividuals = OtherIndividuals(List.fill(25)(otherIndividual))

        val fakeService = new FakeService(otherIndividuals)

        val otherIndividualRows = new AddAnOtherIndividualViewHelper(otherIndividuals.otherIndividuals).rows

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService)
        )).build()

        val request = FakeRequest(GET, getRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutOtherIndividualsView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual
          view(
            otherIndividualRows.inProgress,
            otherIndividualRows.complete,
            otherIndividuals.addToHeading
          )(fakeRequest, messages).toString
        content must include("You cannot enter another individual as you have entered a maximum of 25.")
        content must include("If you have further individuals to add, write to HMRC with their details.")

        application.stop()

      }

      "redirect to add to page and set other individuals to complete when user clicks continue" in {

        val fakeService = new FakeService(otherIndividuals)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request = FakeRequest(POST, submitCompleteRoute)

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse.apply(200)))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9788/maintain-a-trust/overview"

        application.stop()

      }
    }
  }
}
