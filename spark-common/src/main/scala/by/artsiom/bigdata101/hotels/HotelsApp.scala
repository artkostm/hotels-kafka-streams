package by.artsiom.bigdata101.hotels
import org.apache.spark.sql.SparkSession

/**
 * A helper trait for better testability
 * @tparam T - type of the config object
 */
trait HotelsApp[T] extends Serializable {

  def run(config: T)(implicit spark: SparkSession): Unit

  def setup(args: Array[String]): Either[InitError, (SparkSession, T)]

  def main(args: Array[String]): Unit =
    setup(args).fold(error => sys.error(error.usage), {
      case (spark, config) => run(config)(spark)
    })
}

final case class InitError(usage: String)
