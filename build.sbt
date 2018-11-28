name := "hotels-kafka-streams"

lazy val commonSettings = Seq(
  version := "0.5.0",
  scalaVersion := "2.11.12",
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
    "krasserm at bintray".at("http://dl.bintray.com/krasserm/maven"),
    "jitpack".at("https://jitpack.io")
  ),
  libraryDependencies ++= Dependencies.common,
  scalafmtOnCompile := true,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", _ @_*) => MergeStrategy.discard
    case _                           => MergeStrategy.first
  }
)

lazy val root =
  (project in file("."))
    .aggregate(interface, spark_common, generator, batching, streaming, elastic)
    .settings(
      crossScalaVersions := List()
    )

lazy val interface = (project in file("interface")).settings(
  commonSettings
)

lazy val spark_common = (project in file("spark-common"))
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.sparkCommon
  )
  .dependsOn(interface)

lazy val generator = (project in file("generator"))
  .settings(
    commonSettings,
    libraryDependencies ++= Dependencies.generatorModule,
    libraryDependencies ++= Dependencies.commonTest
  )
  .dependsOn(interface)
  .enablePlugins(JavaAppPackaging)

lazy val batching = standardSparkModule(project in file("batching"))
  .settings(
    libraryDependencies ++= Dependencies.batchingModule
  )

lazy val streaming = standardSparkModule(project in file("streaming"))
  .settings(
    libraryDependencies ++= Dependencies.streamingModule
  )

lazy val elastic = standardSparkModule(project in file("elastic"))
  .settings(
    libraryDependencies ++= Dependencies.elastic
  )

lazy val IntegrationTests = config("integTests").extend(Test)

def standardSparkModule(proj: Project): Project =
  proj
    .settings(
      commonSettings,
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
      libraryDependencies ++= Dependencies.test
    )
    .dependsOn(interface, spark_common)
    .enablePlugins(JavaAppPackaging)
    .configs(IntegrationTests)
    .settings(inConfig(IntegrationTests)(Defaults.testSettings): _*)
