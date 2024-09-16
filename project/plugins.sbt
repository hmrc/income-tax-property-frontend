/*
 * Copyright 2024 HM Revenue & Customs
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

resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"

resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)

resolvers += Resolver.typesafeRepo("releases")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"         % "sbt-auto-build"        % "3.22.0")
addSbtPlugin("uk.gov.hmrc"         % "sbt-distributables"    % "2.5.0")
addSbtPlugin("org.playframework"   % "sbt-plugin"            % "3.0.3")
addSbtPlugin("org.scalastyle"     %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scoverage"       % "sbt-scoverage"         % "2.0.9")
addSbtPlugin("io.github.irundaia"  % "sbt-sassify"           % "1.5.2")
addSbtPlugin("net.ground5hark.sbt" % "sbt-concat"            % "0.2.0")
addSbtPlugin("com.typesafe.sbt"    % "sbt-uglify"            % "2.0.0")
addSbtPlugin("com.typesafe.sbt"    % "sbt-digest"            % "1.1.4")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"          % "2.5.2")
addSbtPlugin("ch.epfl.scala"       % "sbt-scalafix"          % "0.12.1")
