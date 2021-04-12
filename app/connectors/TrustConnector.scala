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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.{OtherIndividual, OtherIndividuals, RemoveOtherIndividual, TrustDetails}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class TrustConnector @Inject()(http: HttpClient, config: FrontendAppConfig) {
  
  private val baseUrl: String = s"${config.trustsUrl}/trusts"
  private val otherIndividualsUrl: String = s"$baseUrl/other-individuals"

  private def getTrustDetailsUrl(identifier: String) = s"$baseUrl/$identifier/trust-details"

  def getTrustDetails(identifier: String)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] =
    http.GET[TrustDetails](getTrustDetailsUrl(identifier))

  private def getOtherIndividualsUrl(identifier: String) = s"$otherIndividualsUrl/$identifier/transformed"

  def getOtherIndividuals(identifier: String)
                         (implicit hc: HeaderCarrier, ec : ExecutionContext): Future[OtherIndividuals] =
    http.GET[OtherIndividuals](getOtherIndividualsUrl(identifier))

  private def addOtherIndividualUrl(identifier: String) = s"$otherIndividualsUrl/add/$identifier"

  def addOtherIndividual(identifier: String, otherIndividual: OtherIndividual)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.POST[JsValue, HttpResponse](addOtherIndividualUrl(identifier), Json.toJson(otherIndividual))

  private def amendOtherIndividualUrl(identifier: String, index: Int) = s"$otherIndividualsUrl/amend/$identifier/$index"

  def amendOtherIndividual(identifier: String, index: Int, otherIndividual: OtherIndividual)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.POST[JsValue, HttpResponse](amendOtherIndividualUrl(identifier, index), Json.toJson(otherIndividual))

  private def removeOtherIndividualUrl(identifier: String) = s"$otherIndividualsUrl/$identifier/remove"

  def removeOtherIndividual(identifier: String, otherIndividual: RemoveOtherIndividual)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.PUT[JsValue, HttpResponse](removeOtherIndividualUrl(identifier), Json.toJson(otherIndividual))

  def isTrust5mld(identifier: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"$baseUrl/$identifier/is-trust-5mld"
    http.GET[Boolean](url)
  }
}
