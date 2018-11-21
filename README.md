To generate 10000 (defaul, can be overrided using -Dgenerator.number-of-events=<value>) events and put them into Kafka:

```
java -jar \
    -Dakka.kafka.producer.kafka-clients.bootstrap.servers=192.168.99.100:9092 \
    -Dakka.kafka.producer.topic.name=TestTopic \
    generator.jar
```