To generate 10000 events and put them into Kafka:

```
java -jar \
    -Dakka.kafka.producer.kafka-clients.bootstrap.servers=192.168.99.100:9092 \
    -Dakka.kafka.producer.topic.name=TestTopic \
    -Dakka.kafka.producer.parallelism=10 \
    -Dgenerator.number-of-events=10000 \
    -Dscala.time \
    generator.jar
```

`akka.kafka.producer.parallelism` - tuning parameter of how many sends that can run in parallel
`akka.kafka.producer.kafka-clients.bootstrap.servers` - broker list
`akka.kafka.producer.topic.name` - topic name