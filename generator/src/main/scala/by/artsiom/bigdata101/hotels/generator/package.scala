package by.artsiom.bigdata101.hotels

import akka.Done
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Future

package object generator {
  type Mat     = Future[Done]
  type Message = ProducerRecord[Array[Byte], Array[Byte]]
}
