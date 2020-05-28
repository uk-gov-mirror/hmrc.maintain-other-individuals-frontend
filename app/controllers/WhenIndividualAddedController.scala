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

import controllers.actions.{NameRequiredAction, StandardActionSets}
import forms.{DateOfBirthFormProvider, WhenIndividualAddedFormProvider}
import javax.inject.Inject
import models.Mode
import navigation.{Navigator, OtherIndividualNavigator}
import pages.individual.{DateOfBirthPage, WhenIndividualAddedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.{DateOfBirthView, WhenIndividualAddedView}

import scala.concurrent.{ExecutionContext, Future}

class WhenIndividualAddedController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: PlaybackRepository,
                                       navigator: OtherIndividualNavigator,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       formProvider: WhenIndividualAddedFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WhenIndividualAddedView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider.withPrefix("otherIndividual.whenIndividualAdded")


  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhenIndividualAddedPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.otherIndividual))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForUtr andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.otherIndividual))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhenIndividualAddedPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhenIndividualAddedPage, mode, updatedAnswers))
      )
  }
}
