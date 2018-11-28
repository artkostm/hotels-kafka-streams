package by.artsiom.bigdata101.hotels

import java.io.ByteArrayOutputStream
import java.sql.{Date, Timestamp}
import java.util.{Date => UDate}

import by.artsiom.bigdata101.hotels.model.Event
import com.sksamuel.avro4s.{AvroOutputStream, AvroSchema}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class HotelImplicitsSpec extends FlatSpec with BeforeAndAfterAll {

  var spark: SparkSession = _
  var testDf: DataFrame = _
  val event = testEvent()

  override protected def beforeAll(): Unit = {
    val sparkSession = SparkSession.builder
      .appName("spark-common-tests")
      .master("local")
      .getOrCreate()

    spark = sparkSession

    import sparkSession.implicits._

    testDf = spark.sparkContext.parallelize(List(toAvro(event))).toDF("event")
  }

  it should "convert Dataframe to Dataset[Event] correctly" in {
    implicit val sparkSession = spark
    val hotelImplicits = new HotelImplicits {}
    import hotelImplicits._

    testDf.fromAvro("event").collect().foreach { actualEvent =>
      assert(actualEvent.productIterator.mkString === event.productIterator.mkString)
    }
  }

  override protected def afterAll(): Unit = if (spark != null) {
    spark.stop()
  }

  def toAvro(event: Event): Array[Byte] = {
    val baos   = new ByteArrayOutputStream()
    val output = AvroOutputStream.binary[Event].to(baos).build(AvroSchema[Event])
    output.write(event)
    output.close()
    baos.toByteArray
  }

  def testEvent() = Event(
    new Timestamp(new UDate().getTime),
    1, 2, 3, 4, 5, 6.0F, 7, true, false,
    8, new Date(new UDate().getTime),
    new Date(new UDate().getTime), 9, 10,
    11, 12, 13, false, 14, 15, 16, 17, 18
  )
}
