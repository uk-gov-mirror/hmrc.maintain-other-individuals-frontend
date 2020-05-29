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

package pages.behaviours

import models.{IdCard, NonUkAddress, Passport, UkAddress, UserAnswers}
import pages.individual._


class NationalInsuranceNumberYesNoPageSpec extends PageBehaviours {

  "NationalInsuranceNumberYesNoPage" must {

    beRetrievable[Boolean](NationalInsuranceNumberYesNoPage)

    beSettable[Boolean](NationalInsuranceNumberYesNoPage)

    beRemovable[Boolean](NationalInsuranceNumberYesNoPage)

    "implement cleanup logic when NO selected" in {
      val userAnswers = UserAnswers("id", "utr", LocalDate.now)
        .set(NationalInsuranceNumberYesNoPage, false)


      userAnswers.get.get(UkAddressPage) mustNot be(defined)
    }

    "implement cleanup logic when YES selected" in {
      val userAnswers = UserAnswers("id", "utr", LocalDate.now)
        .set(NationalInsuranceNumberPage, "AA12345A")
        .flatMap(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode"))
        .flatMap(_.set(NonUkAddressPage, NonUkAddress("line1", "line2", None, "Germany")))
        .flatMap(_.set(PassportDetailsYesNoPage, true))
        .flatMap(_.set(PassportDetailsPage, Passport("GB", "1234567", LocalDate.now)))
        .flatMap(_.set(IdCardDetailsYesNoPage, true ))
        .flatMap(_.set(IdCardDetailsPage, IdCard("Germany", "1234567", LocalDate.now)))


      userAnswers.get.get(UkAddressPage) mustNot be(defined)
      userAnswers.get.get(NonUkAddressPage) mustNot be(defined)
      userAnswers.get.get(PassportDetailsPage) mustNot be(defined)
      userAnswers.get.get(IdCardDetailsPage) mustNot be(defined)
    }
  }
}
