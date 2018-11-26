name := "hotels-kafka-streams"

lazy val commonSettings = Seq(
  version := "0.5.0",
  scalaVersion := "2.12.7",
  scalacOptions := Seq(
    "-feature",
    "-encoding",
    //"-deprecation",
    "UTF-8",
    "-language:higherKinds",
    "-language:existentials",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-Ypartial-unification"
  ),
  resolvers ++= Seq(
    "Twitter Maven".at("https://maven.twttr.com"),
    Resolver.bintrayRepo("jmcardon", "tsec"),
    Resolver.bintrayRepo("akka", "maven"),
    "Sonatype OSS Snapshots".at("https://oss.sonatype.org/content/repositories/snapshots"),
    "krasserm at bintray".at("http://dl.bintray.com/krasserm/maven")
  ),
  libraryDependencies ++= Dependencies.common,
  scalafmtOnCompile := true
)

lazy val root =
  (project in file(".")).aggregate(interface, generator, batching, streaming)

lazy val interface = (project in file("interface")).settings(
  commonSettings
)

lazy val generator = (project in file("generator"))
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.generatorModule
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)

lazy val batching = (project in file("batching"))
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.batchingModule,
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)

lazy val streaming = (project in file("streaming"))
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.streamingModule,
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)

assemblyMergeStrategy in assembly := {
    case PathList("org", "apache", _*) => MergeStrategy.last
    case PathList("com", "google", _*) => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
}