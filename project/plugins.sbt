resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"

resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)

resolvers += Resolver.typesafeRepo("releases")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"        % "sbt-auto-build"        % "3.24.0")
addSbtPlugin("uk.gov.hmrc"        % "sbt-distributables"    % "2.6.0")
addSbtPlugin("org.playframework"  % "sbt-plugin"            % "3.0.10")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"         % "2.4.4")
addSbtPlugin("io.github.irundaia" % "sbt-sassify"           % "1.5.2")
addSbtPlugin("com.github.sbt"     % "sbt-concat"            % "1.0.0")
addSbtPlugin("com.github.sbt"     % "sbt-digest"            % "2.0.0")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"           % "0.6.4")
