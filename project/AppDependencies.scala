import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val silencerVersion = "1.6.0"
  // Use the silencer plugin to suppress warnings from unused imports in compiled twirl templates
  val unusedRoutesImportSilencer = Seq(
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"            % "0.18.8-play26",
    "uk.gov.hmrc"       %% "logback-json-logger"            % "5.1.0",
    "uk.gov.hmrc"       %% "govuk-template"                 % "5.55.0-play-26",
    "uk.gov.hmrc"       %% "play-health"                    % "3.15.0-play-26",
    "uk.gov.hmrc"       %% "domain"                         % "5.9.0-play-26",
    "com.typesafe.play" %% "play-json-joda"                 % "2.7.4",
    "uk.gov.hmrc"       %% "play-ui"                        % "9.0.0-play-26",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.2.0-play-26",
    "uk.gov.hmrc"       %% "bootstrap-play-26"              % "3.4.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"               %% "scalatest"              % "3.0.8",
    "org.scalatestplus.play"      %% "scalatestplus-play"     % "3.1.3",
    "org.pegdown"                 %  "pegdown"                % "1.6.0",
    "org.jsoup"                   %  "jsoup"                  % "1.12.1",
    "wolfendale"                  %% "scalacheck-gen-regexp"  % "0.1.2",
    "com.typesafe.play"           %% "play-test"              % PlayVersion.current,
    "org.mockito"                 %  "mockito-all"            % "1.10.19",
    "org.scalacheck"              %% "scalacheck"             % "1.14.3",
    "com.github.tomakehurst"      % "wiremock-standalone"     % "2.25.1"
  )

  def apply(): Seq[ModuleID] = compile ++ unusedRoutesImportSilencer ++ test

  val akkaVersion = "2.5.23"
  val akkaHttpVersion = "10.0.15"

  val overrides: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  )
}
