import sbt._

object Dependencies {
  val versions = new {
    val randomDataGenerator = "2.6"
    val alpakka = "0.22"
  }

  lazy val common = Seq(
    )

  lazy val generatorModule = Seq(
    "com.danielasfregola" %% "random-data-generator" % versions.randomDataGenerator,
    "com.typesafe.akka" %% "akka-stream-kafka" % versions.alpakka
  )
}
