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
  scalafmtOnCompile := true,
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", _ @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)

lazy val root =
  (project in file(".")).aggregate(interface_11, interface_12, spark_common, generator, batching, streaming)
    .settings(
      crossScalaVersions := List()
    )

lazy val interface_11 = (project in file("interface_11")).settings(
  commonSettings,
  scalaVersion := "2.11.12"
)

lazy val interface_12 = (project in file("interface_12")).settings(
  commonSettings,
  scalaVersion := "2.12.7"
)

lazy val spark_common = (project in file("spark-common"))
  .settings(
    commonSettings,
    scalaVersion := "2.11.12",
    libraryDependencies ++= Dependencies.sparkCommon
  )
  .dependsOn(interface_11)

lazy val generator = (project in file("generator"))
  .settings(
    commonSettings,
    scalaVersion := "2.12.7",
    libraryDependencies ++= Dependencies.generatorModule
  )
  .dependsOn(interface_12)
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

def standardSparkModule(proj: Project): Project = 
  proj
    .settings(
      commonSettings,
      scalaVersion := "2.11.12",
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
    )
    .dependsOn(interface_11, spark_common)
    .enablePlugins(JavaAppPackaging)