name := "hotels-kafka-streams"

lazy val commonSettings = Seq(
  version := "0.5.0",
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
    Resolver.bintrayRepo("akka", "maven"),
    "Sonatype OSS Snapshots".at("https://oss.sonatype.org/content/repositories/snapshots"),
    "krasserm at bintray".at("http://dl.bintray.com/krasserm/maven")
  ),
  libraryDependencies ++= Dependencies.common,
  scalafmtOnCompile := true
)

lazy val root =
  (projectMatrix in file(".")).aggregate(interface, spark_common, generator, batching, streaming)

lazy val interface = (projectMatrix in file("interface"))
  .scalaVersions("2.12.7", "2.11.12")
  .settings(
  commonSettings
)
  .jvmPlatform()

lazy val spark_common = (projectMatrix in file("spark-common"))
  .scalaVersions("2.11.12")
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.sparkCommon
  )
  .dependsOn(interface)
  .jvmPlatform()

lazy val generator = (projectMatrix in file("generator"))
  .scalaVersions("2.12.7")
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.generatorModule
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)
  .jvmPlatform()

lazy val batching = standardSparkModule(projectMatrix in file("batching"))
  .settings(
    libraryDependencies ++= Dependencies.batchingModule
  )

lazy val streaming = standardSparkModule(projectMatrix in file("streaming"))
  .settings(
    libraryDependencies ++= Dependencies.streamingModule
  )

lazy val elastic = standardSparkModule(projectMatrix in file("elastic"))
  .settings(
    libraryDependencies ++= Dependencies.elastic
  )

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", _*)                        => MergeStrategy.last
  case PathList("com", "google", _*)                        => MergeStrategy.last
  case "log4j.properties"                                   => MergeStrategy.last
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

def standardSparkModule(proj: ProjectMatrix): ProjectMatrix =
  proj
    .scalaVersions("2.11.12")
    .settings(
      commonSettings,
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
    )
    .dependsOn(interface, spark_common)
    .enablePlugins(JavaAppPackaging)
    .jvmPlatform()
