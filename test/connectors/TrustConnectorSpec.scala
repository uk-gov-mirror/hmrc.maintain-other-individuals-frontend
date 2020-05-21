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

package connectors

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.{Name, OtherIndividual, OtherIndividuals, TrustDetails}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

class TrustConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  "trust connector" when {

    "get trusts details" in {

      val utr = "1000000008"

      val json = Json.parse(
        """
          |{
          | "startDate": "1920-03-28",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustConnector]

      server.stubFor(
        get(urlEqualTo(s"/trusts/$utr/trust-details"))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(utr)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = LocalDate.parse("1920-03-28"))
      }
    }

    "get other individuals returns a trust with empty lists" must {

      "return a default empty list of other individuals" in {

        val utr = "1000000008"

        val json = Json.parse(
          """
            |{
            | "naturalPerson": [
            | ]
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$utr/transformed/other-individuals"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getOtherIndividuals(utr)

        whenReady(processed) {
          result =>
            result mustBe OtherIndividuals(otherIndividuals = Nil)
        }

        application.stop()
      }

    }

    "get other individuals" must {

      "parse the response and return the other individuals" in {
        val utr = "1000000008"

        val json = Json.parse(
          """
            |{
            | "naturalPerson" : [
            |     {
            |       "lineNo" : "79",
            |       "name" : {
            |         "firstName" : "Other",
            |         "lastName" : "Individual"
            |       },
            |       "entityStart" : "2019-09-23"
            |     }
            |   ]
            |}
            |""".stripMargin)

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustConnector]

        server.stubFor(
          get(urlEqualTo(s"/trusts/$utr/transformed/other-individuals"))
            .willReturn(okJson(json.toString))
        )

        val processed = connector.getOtherIndividuals(utr)

        whenReady(processed) {
          result =>
            result mustBe
              OtherIndividuals(otherIndividuals = List(
                OtherIndividual(
                  name = Name("Other", None, "Individual"),
                  dateOfBirth = None,
                  identification = None,
                  address = None,
                  entityStart = LocalDate.parse("2019-09-23"),
                  provisional = false
                )
              )
            )
        }

        application.stop()
      }
    }
  }
}
