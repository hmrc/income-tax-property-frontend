import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.1.0"
  private val hmrcMongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"            % "12.8.0",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping-play-30" % "3.3.0",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "com.github.tototoshi" %% "scala-csv"                             % "2.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.19",
    "org.scalatestplus"   %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"   %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"         %% "mockito-scala"           % "2.0.0",
    "org.scalamock"       %% "scalamock"               % "7.4.1",
    "org.scalacheck"      %% "scalacheck"              % "1.18.1",
    "org.jsoup"            % "jsoup"                   % "1.21.2",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
