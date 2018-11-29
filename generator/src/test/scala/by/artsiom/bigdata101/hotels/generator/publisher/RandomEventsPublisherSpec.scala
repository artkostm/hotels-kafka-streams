package by.artsiom.bigdata101.hotels.generator.publisher

import by.artsiom.bigdata101.hotels.model.Event
import org.reactivestreams.{Publisher, Subscriber, Subscription}
import org.reactivestreams.tck.{PublisherVerification, TestEnvironment}

/**
 * https://github.com/reactive-streams/reactive-streams-jvm/tree/master/tck
 *
 * TCK is to guide and help Reactive Streams library implementers to validate
 * their implementations against the rules defined in the Specification.
 *
 */
class RandomEventsPublisherSpec extends PublisherVerification[Event](new TestEnvironment()) {
  override def createPublisher(elements: Long): Publisher[Event] =
    RandomEventsPublisher(elements.toInt)

  override def createFailedPublisher(): Publisher[Event] = new Publisher[Event] {
    override def subscribe(s: Subscriber[_ >: Event]): Unit = {
      s.onSubscribe(new Subscription {
        override def request(n: Long): Unit = ()
        override def cancel(): Unit         = ()
      })
      s.onError(new RuntimeException)
    }
  }
}
