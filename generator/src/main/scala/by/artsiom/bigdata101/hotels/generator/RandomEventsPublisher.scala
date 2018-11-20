package by.artsiom.bigdata101.hotels.generator

import java.util.concurrent.atomic.AtomicBoolean

import by.artsiom.bigdata101.hotels.model.Event
import com.danielasfregola.randomdatagenerator.RandomDataGenerator
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.util.Try

trait RandomEventsPublisher extends Publisher[Event] with RandomDataGenerator

object RandomEventsPublisher {

  /**
    * A {@link RandomEventsPublisher} is a provider of a bounded number of random sequenced events
    *
    * @param numberOfEvents - the number of events to emit
    * Usage:
    *  {{{
    *    akka.stream.scaladsl.Source.fromPublisher(RandomEventsPublisher(10))...
    *  }}}
    */
  def apply(numberOfEvents: Int): RandomEventsPublisher =
    new RandomEventsPublisher() {
    override def subscribe(s: Subscriber[_ >: Event]): Unit =
      Try{
        s.onSubscribe(RandomEventsSubscription)
        random[Event](numberOfEvents).foreach(s.onNext(_))
      }
      .fold(s.onError(_), _ => s.onComplete())
  }

  private object RandomEventsSubscription extends Subscription {
    override def request(n: Long): Unit = ()
    override def cancel(): Unit = ()
  }
}