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

package config

import java.net.{URI, URLEncoder}
import java.time.LocalDate

import com.google.inject.{Inject, Singleton}
import controllers.routes
import play.api.Configuration
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Call, Request}

@Singleton
class FrontendAppConfig @Inject() (val configuration: Configuration) {

  final val ENGLISH = "en"
  final val WELSH = "cy"
  final val UK_COUNTRY_CODE = "GB"

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "trusts"

  val analyticsHost: String = configuration.get[String](s"google-analytics.host")

  val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  val betaFeedbackUnauthenticatedUrl = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  lazy val authUrl: String = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  lazy val countdownLength: String = configuration.get[String]("timeout.countdown")
  lazy val timeoutLength: String = configuration.get[String]("timeout.length")

  lazy val trustsUrl: String = configuration.get[Service]("microservice.services.trusts").baseUrl

  lazy val trustAuthUrl: String = configuration.get[Service]("microservice.services.trusts-auth").baseUrl

  lazy val trustsStoreUrl: String = configuration.get[Service]("microservice.services.trusts-store").baseUrl

  lazy val locationCanonicalList: String = configuration.get[String]("location.canonical.list.all")
  lazy val locationCanonicalListCY: String = configuration.get[String]("location.canonical.list.allCY")

  lazy val logoutUrl: String = configuration.get[String]("urls.logout")

  lazy val logoutAudit: Boolean =
    configuration.get[Boolean]("microservice.services.features.auditing.logout")

  lazy val maintainATrustOverview: String = configuration.get[String]("urls.maintainATrustOverview")

  lazy val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("microservice.services.features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang(ENGLISH),
    "cymraeg" -> Lang(WELSH)
  )

  def routeToSwitchLanguage: String => Call =
    (lang: String) => routes.LanguageSwitchController.switchToLanguage(lang)


  private lazy val accessibilityBaseLinkUrl: String = configuration.get[String]("urls.accessibility")

  def accessibilityLinkUrl(implicit request: Request[_]): String = {
    val userAction = URLEncoder.encode(new URI(request.uri).getPath, "UTF-8")
    s"$accessibilityBaseLinkUrl?userAction=$userAction"
  }

  private val minDay: Int = configuration.get[Int]("dates.minimum.day")
  private val minMonth: Int = configuration.get[Int]("dates.minimum.month")
  private val minYear: Int = configuration.get[Int]("dates.minimum.year")
  lazy val minDate: LocalDate = LocalDate.of(minYear, minMonth, minDay)

  private val maxDay: Int = configuration.get[Int]("dates.maximum.day")
  private val maxMonth: Int = configuration.get[Int]("dates.maximum.month")
  private val maxYear: Int = configuration.get[Int]("dates.maximum.year")
  lazy val maxDate: LocalDate = LocalDate.of(maxYear, maxMonth, maxDay)

  def helplineUrl(implicit messages: Messages): String = {
    val path = messages.lang.code match {
      case WELSH => "urls.welshHelpline"
      case _ => "urls.trustsHelpline"
    }

    configuration.get[String](path)
  }
}
