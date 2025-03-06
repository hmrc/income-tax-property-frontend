import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.10.0"
  private val hmrcMongoVersion = "2.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"            % "10.12.0",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "com.github.tototoshi" %% "scala-csv"                             % "1.3.10"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.18",
    "org.scalatestplus"   %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus"   %% "mockito-3-4"             % "3.2.10.0",
    "org.mockito"         %% "mockito-scala"           % "1.17.31",
    "org.scalamock"       %% "scalamock"               % "5.2.0",
    "org.scalacheck"      %% "scalacheck"              % "1.18.0",
    "org.pegdown"          % "pegdown"                 % "1.6.0",
    "org.jsoup"            % "jsoup"                   % "1.17.2",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
