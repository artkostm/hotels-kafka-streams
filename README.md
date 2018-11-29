Create kafka topic (with the name AvroTopic) using:

```
kafka-topics.sh --create --zookeeper 192.168.99.100:2181 --replication-factor 1 --partitions 3 --topic AvroTopic
```

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

Download mpack from https://community.hortonworks.com/storage/attachments/87415-elasticsearch-mpack-2500-9.tar.gz to ambari-server node (/home/root)

Install Management Pack ```sudo ambari-server install-mpack --mpack=/home/root/87415-elasticsearch-mpack-2500-9.tar.gz --verbose```

(to uninstall use ```sudo ambari-server uninstall-mpack --mpack-name=elasticsearch-ambari.mpack```). Then restart Ambari Server ```sudo ambari-server restart```

The following settings will be required during the Install Wizard



The Create Index API is used to manually create an index in Elasticsearch. All documents in Elasticsearch are stored inside of one index or another.

Basic command in our case is the following:

```
curl -X PUT "localhost:9200/events" -H 'Content-Type: application/json' -d'
{
    "mappings": {
        "event": {
          "properties": {
            "channel": {
              "type": "long"
            },
            "cnt": {
              "type": "long"
            },
            "dateTime": {
              "type": "date",
    		  "format": "epoch_millis"
            },
            "hotelCluster": {
              "type": "long"
            },
            "hotelContinent": {
              "type": "long"
            },
            "hotelCountry": {
              "type": "long"
            },
            "hotelMarket": {
              "type": "long"
            },
            "isBooking": {
              "type": "boolean"
            },
            "isMobile": {
              "type": "boolean"
            },
            "isPackage": {
              "type": "boolean"
            },
            "origDestinationDistance": {
              "type": "float"
            },
            "posaContinent": {
              "type": "long"
            },
            "siteName": {
              "type": "long"
            },
            "srchAdultsCnt": {
              "type": "long"
            },
            "srchChildrenCnt": {
              "type": "long"
            },
            "srchCi": {
              "type": "long"
            },
            "srchCo": {
              "type": "long"
            },
            "srchDestinationId": {
              "type": "long"
            },
            "srchDestinationTypeId": {
              "type": "long"
            },
            "srchRmCnt": {
              "type": "long"
            },
            "userId": {
              "type": "long"
            },
            "userLocationCity": {
              "type": "long"
            },
            "userLocationCountry": {
              "type": "long"
            },
            "userLocationRegion": {
              "type": "long"
            }
          }
        }
     }
}
'
```

Run spark-submit:

```
spark-submit --class by.artsiom.bigdata101.hotels.elastic.Main --master local[*] elastic.jar 192.168.99.100:9092 AvroTopic events/event localhost
```

Where `192.168.99.100:9092` is the broker list, `AvroTopic` is the name of the kafka topic, `events/event` is the elasticsearch' index/type pair, and `localhost` - Elasticsearch node address.

Then run the event generator to see new events using Kibana.