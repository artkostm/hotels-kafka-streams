name := "hotels-kafka-streams"

lazy val commonSettings = Seq(
  version := "0.5.0",
  scalaVersion := "2.12.7",
  scalacOptions := Seq(
    "-feature",
    "-encoding",
    "-deprecation",
    "UTF-8",
    "-language:higherKinds",
    "-language:existentials",
    "-language:implicitConversions",
    "-Ypartial-unification"
  ),
  resolvers ++= Seq(
    "Twitter Maven" at "https://maven.twttr.com",
    Resolver.bintrayRepo("jmcardon", "tsec"),
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"
  ),
  libraryDependencies ++= Dependencies.common,
  scalafmtOnCompile := true
)

lazy val root =
  (project in file(".")).aggregate(interface, generator, batching, streaming)

lazy val interface = (project in file("interface")).settings(
  commonSettings,
)

lazy val generator = (project in file("generator"))
  .settings(
    commonSettings
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)

lazy val batching = (project in file("batching"))
  .settings(
    commonSettings
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)

lazy val streaming = (project in file("streaming"))
  .settings(
    commonSettings
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)
