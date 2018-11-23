package by.artsiom.bigdata101.hotels.generator.publisher

import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import by.artsiom.bigdata101.hotels.model.Event
import com.danielasfregola.randomdatagenerator.RandomDataGenerator
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.util.Try

trait RandomEventsPublisher extends Publisher[Event]

object RandomEventsPublisher extends GenUtils {

  /**
   * A {@link RandomEventsPublisher} is a provider of a bounded number of random sequenced events
   *
   * @param numberOfEvents - the number of events to emit
   *
   * Usage:
   *  {{{
   *    akka.stream.scaladsl.Source.fromPublisher(RandomEventsPublisher(10))...
   *  }}}
   */
  def apply(numberOfEvents: Int): RandomEventsPublisher =
    new RandomEventsPublisher() {
      override def subscribe(s: Subscriber[_ >: Event]): Unit =
        s.onSubscribe(new RandomEventsSubscription(s, numberOfEvents))
    }

  private class RandomEventsSubscription(s: Subscriber[_ >: Event], numberOfEvents: Int)
      extends Subscription
      with RandomDataGenerator {
    // stream iterator
    val iterator   = random[Event](numberOfEvents).toIterator
    val terminated = new AtomicBoolean(false)
    val demand     = new AtomicLong()

    override def request(n: Long): Unit =
      n match {
        case num if num < 0 && !terminate() =>
          s.onError(new IllegalArgumentException("Negative subscription request!"))
        case _ if demand.getAndAdd(n) > 0 => ()
        case _ =>
          Try(while (demand.get() > 0 && iterator.hasNext && !terminated.get()) {
            s.onNext(iterator.next())
            demand.decrementAndGet()
          }).fold(err => if (!terminate()) s.onError(err), _ => if (!iterator.hasNext && !terminate()) s.onComplete())
      }

    override def cancel(): Unit = terminate()

    private def terminate(): Boolean =
      terminated.getAndSet(true)
  }
}
