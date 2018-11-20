import sbt._

object Dependencies {
  val versions = new {
    val randomDataGenerator = "2.6"
  }

  lazy val common = Seq(
    )

  lazy val generatorModule = Seq(
    "com.danielasfregola" %% "random-data-generator" % versions.randomDataGenerator
  )
}
