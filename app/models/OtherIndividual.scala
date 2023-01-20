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

package models

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class OtherIndividual(name: Name,
                                 dateOfBirth: Option[LocalDate],
                                 countryOfNationality: Option[String],
                                 countryOfResidence: Option[String],
                                 identification: Option[IndividualIdentification],
                                 address : Option[Address],
                                 mentalCapacityYesNo: Option[YesNoDontKnow],
                                 entityStart: LocalDate,
                                 provisional : Boolean)

object OtherIndividual {

  implicit val reads: Reads[OtherIndividual] =
    ((__ \ 'name).read[Name] and
      (__ \ 'dateOfBirth).readNullable[LocalDate] and
      (__ \ 'nationality).readNullable[String] and
      (__ \ 'countryOfResidence).readNullable[String] and
      __.lazyRead(readNullableAtSubPath[IndividualIdentification](__ \ 'identification)) and
      __.lazyRead(readNullableAtSubPath[Address](__ \ 'identification \ 'address)) and
      readMentalCapacity and
      (__ \ "entityStart").read[LocalDate] and
      (__ \ "provisional").readWithDefault(false)).tupled.map{

      case (name, dob, countryOfNationality, countryOfResidence, nino, identification, mentalCapacity, entityStart, provisional) =>
        OtherIndividual(name, dob, countryOfNationality, countryOfResidence, nino, identification, mentalCapacity, entityStart, provisional)

    }

  implicit val writes: Writes[OtherIndividual] =
    ((__ \ 'name).write[Name] and
      (__ \ 'dateOfBirth).writeNullable[LocalDate] and
      (__ \ 'nationality).writeNullable[String] and
      (__ \ 'countryOfResidence).writeNullable[String] and
      (__ \ 'identification).writeNullable[IndividualIdentification] and
      (__ \ 'identification \ 'address).writeNullable[Address] and
      (__ \ 'legallyIncapable).writeNullable[YesNoDontKnow](writeMentalCapacity) and
      (__ \ "entityStart").write[LocalDate] and
      (__ \ "provisional").write[Boolean]
      ).apply(unlift(OtherIndividual.unapply))

  private def readNullableAtSubPath[T:Reads](subPath : JsPath) : Reads[Option[T]] = Reads (
    _.transform(subPath.json.pick)
      .flatMap(_.validate[T])
      .map(Some(_))
      .recoverWith(_ => JsSuccess(None))
  )

  private def readMentalCapacity: Reads[Option[YesNoDontKnow]] =
    (__ \ 'legallyIncapable).readNullable[Boolean].flatMap[Option[YesNoDontKnow]] { x: Option[Boolean] =>
      Reads(_ => JsSuccess(YesNoDontKnow.fromBoolean(x)))
    }

  private def writeMentalCapacity: Writes[YesNoDontKnow] = {
    case YesNoDontKnow.Yes => JsBoolean(false)
    case YesNoDontKnow.No => JsBoolean(true)
    case _ => JsNull
  }
}