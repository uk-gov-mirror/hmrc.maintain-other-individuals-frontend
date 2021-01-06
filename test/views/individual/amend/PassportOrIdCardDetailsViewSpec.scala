/*
 * Copyright 2021 HM Revenue & Customs
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

package views.individual.amend

import base.FakeTrustsApp
import controllers.individual.amend.routes
import forms.CombinedPassportOrIdCardDetailsFormProvider
import models.{CombinedPassportOrIdCard, Name}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.InputOption
import utils.countryOptions.CountryOptions
import views.behaviours.QuestionViewBehaviours
import views.html.individual.amend.PassportOrIdCardDetailsView

class PassportOrIdCardDetailsViewSpec extends QuestionViewBehaviours[CombinedPassportOrIdCard] with FakeTrustsApp {

  private val messageKeyPrefix: String = "otherIndividual.passportOrIdCardDetails"
  private val name: Name = Name("First", Some("Middle"), "Last")

  override val form: Form[CombinedPassportOrIdCard] = new CombinedPassportOrIdCardDetailsFormProvider(frontendAppConfig).withPrefix(messageKeyPrefix)

  "PassportOrIdCardDetails View" must {

    val view = viewFor[PassportOrIdCardDetailsView](Some(emptyUserAnswers))

    val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptions].options

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, name.displayName, countryOptions)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name.displayName)

    behave like pageWithBackLink(applyView(form))

    "fields" must {

      behave like pageWithPassportOrIDCardDetailsFields(
        form,
        applyView,
        messageKeyPrefix,
        routes.PassportOrIdCardDetailsController.onSubmit().url,
        Seq(("country", None), ("number", None)),
        "expiryDate",
        name.displayName
      )
    }

    behave like pageWithASubmitButton(applyView(form))
  }
}
