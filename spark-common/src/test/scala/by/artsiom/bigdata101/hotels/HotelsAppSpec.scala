package by.artsiom.bigdata101.hotels

import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpec

class HotelsAppSpec extends FlatSpec {
  import HotelsAppSpec._

  final case class TestConfig(param1: String, param2: String)

  def withCorrectParams(param1: String, param2: String)(testCode: HotelsApp[TestConfig] => Unit): Unit =
    testCode(new HotelsApp[TestConfig] {
      override def run(spark: SparkSession, config: TestConfig): Unit = {
        assert(spark == null)
        assert(config.param1 == param1)
        assert(config.param2 == param2)
      }
      override def setup(args: Array[String]): Either[InitError, (SparkSession, TestConfig)] =
        args match {
          case Array(p1, p2) => Right((null, TestConfig(p1, p2)))
          case _ => fail("have to fail this")
        }
    })

  "HotelsApp" should "run app for correct config params" in withCorrectParams(Param1, Param2) {
    _.main(Array(Param1, Param2))
  }

  "HotelsApp" should "throw an exception for wrong params" in withCorrectParams(Param1, Param2) {
    app => intercept[Exception](app.main(Array(Param1, Param2, Param3)))
  }
}

object HotelsAppSpec {
  val Param1 = "param1"
  val Param2 = "param2"
  val Param3 = "param3"
}