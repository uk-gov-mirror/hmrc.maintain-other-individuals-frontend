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

package utils.print

import com.google.inject.Inject
import models.{CheckMode, NormalMode, UserAnswers}
import pages.individual.{AddressYesNoPage, DateOfBirthPage, DateOfBirthYesNoPage, IdCardDetailsPage, IdCardDetailsYesNoPage, LiveInTheUkYesNoPage, NamePage, NationalInsuranceNumberPage, NationalInsuranceNumberYesNoPage, NonUkAddressPage, PassportDetailsPage, PassportDetailsYesNoPage, UkAddressPage, WhenIndividualAddedPage}
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, AnswerSection}

class OtherIndividualPrintHelper @Inject()(answerRowConverter: AnswerRowConverter,
                                           countryOptions: CountryOptions
                                            ) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, protectorName: String)(implicit messages: Messages) = {

    val bound = answerRowConverter.bind(userAnswers, protectorName, countryOptions)

    val add: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "individualProtector.name", controllers.routes.NameController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(DateOfBirthYesNoPage, "individualProtector.dateOfBirthYesNo", controllers.routes.DateOfBirthYesNoController.onPageLoad(NormalMode).url),
      bound.dateQuestion(DateOfBirthPage, "individualProtector.dateOfBirth", controllers.routes.DateOfBirthController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "individualProtector.nationalInsuranceNumberYesNo", controllers.routes.NationalInsuranceNumberYesNoController.onPageLoad(NormalMode).url),
      bound.ninoQuestion(NationalInsuranceNumberPage, "individualProtector.nationalInsuranceNumber", controllers.routes.NationalInsuranceNumberController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(AddressYesNoPage, "individualProtector.addressYesNo", controllers.routes.AddressYesNoController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "individualProtector.liveInTheUkYesNo", controllers.routes.LiveInTheUkYesNoController.onPageLoad(NormalMode).url),
      bound.addressQuestion(UkAddressPage, "individualProtector.ukAddress", controllers.routes.UkAddressController.onPageLoad(NormalMode).url),
      bound.addressQuestion(NonUkAddressPage, "individualProtector.nonUkAddress",controllers.routes.NonUkAddressController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(PassportDetailsYesNoPage, "individualProtector.passportDetailsYesNo", controllers.routes.PassportDetailsYesNoController.onPageLoad(NormalMode).url),
      bound.passportDetailsQuestion(PassportDetailsPage, "individualProtector.passportDetails", controllers.routes.PassportDetailsController.onPageLoad(NormalMode).url),
      bound.yesNoQuestion(IdCardDetailsYesNoPage, "individualProtector.idCardDetailsYesNo", controllers.routes.IdCardDetailsYesNoController.onPageLoad(NormalMode).url),
      bound.idCardDetailsQuestion(IdCardDetailsPage, "individualProtector.idCardDetails", controllers.routes.IdCardDetailsController.onPageLoad(NormalMode).url),
      bound.dateQuestion(WhenIndividualAddedPage, "individualProtector.startDate", controllers.routes.WhenIndividualAddedController.onPageLoad().url)
    ).flatten

    val amend: Seq[AnswerRow] = Seq(
      bound.nameQuestion(NamePage, "individualProtector.name", controllers.routes.NameController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(DateOfBirthYesNoPage, "individualProtector.dateOfBirthYesNo", controllers.routes.DateOfBirthYesNoController.onPageLoad(CheckMode).url),
      bound.dateQuestion(DateOfBirthPage, "individualProtector.dateOfBirth", controllers.routes.DateOfBirthController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(NationalInsuranceNumberYesNoPage, "individualProtector.nationalInsuranceNumberYesNo", controllers.routes.NationalInsuranceNumberYesNoController.onPageLoad(CheckMode).url),
      bound.ninoQuestion(NationalInsuranceNumberPage, "individualProtector.nationalInsuranceNumber", controllers.routes.NationalInsuranceNumberController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(AddressYesNoPage, "individualProtector.addressYesNo", controllers.routes.AddressYesNoController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(LiveInTheUkYesNoPage, "individualProtector.liveInTheUkYesNo", controllers.routes.LiveInTheUkYesNoController.onPageLoad(CheckMode).url),
      bound.addressQuestion(UkAddressPage, "individualProtector.ukAddress", controllers.routes.UkAddressController.onPageLoad(CheckMode).url),
      bound.addressQuestion(NonUkAddressPage, "individualProtector.nonUkAddress", controllers.routes.NonUkAddressController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(PassportDetailsYesNoPage, "individualProtector.passportDetailsYesNo", controllers.routes.PassportDetailsYesNoController.onPageLoad(CheckMode).url),
      bound.passportDetailsQuestion(PassportDetailsPage, "individualProtector.passportDetails", controllers.routes.PassportDetailsController.onPageLoad(CheckMode).url),
      bound.yesNoQuestion(IdCardDetailsYesNoPage, "individualProtector.idCardDetailsYesNo", controllers.routes.IdCardDetailsYesNoController.onPageLoad(CheckMode).url),
      bound.idCardDetailsQuestion(IdCardDetailsPage, "individualProtector.idCardDetails", controllers.routes.IdCardDetailsController.onPageLoad(CheckMode).url)
    ).flatten

    AnswerSection(
      None,
      if (provisional) add else amend
    )


  }
}
