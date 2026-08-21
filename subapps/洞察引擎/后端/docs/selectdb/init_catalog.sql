CREATE ROUTINE LOAD test_topic_kafka ON test_topic
COLUMNS(code, name)
PROPERTIES(
    "enclose"="\"",
    "escape"="\\",
    "format"="json",
    "jsonpaths"="[\"$.code\",\"$.name\"]"
)
FROM KAFKA
(
    "kafka_broker_list" = "10.62.133.17:29095",
    "kafka_topic" = "test_topic",
    -- "kafka_partitions" = "0",
    "property.group.id" = "cqca-voc-selectdb",
    "property.kafka_default_offsets" = "OFFSET_BEGINNING"
);



CREATE  CATALOG voc_mysql_jdbc
PROPERTIES
(
    "type" = "jdbc",
    "user" = "root",
    "password" = "L7bzd1gmm+db",
    "jdbc_url" = "jdbc:mysql://10.62.133.17:33306",
    "driver_url" = "mysql-connector-java-8.0.25.jar",
    "driver_class" = "com.mysql.cj.jdbc.Driver"
);



--  select * from voc_mysql_jdbc.voc_ms_be.ins_car_series_info
CREATE CATALOG voc_mysql_jdbc
PROPERTIES
(
    "type" = "jdbc",
    "user" = "root",
    "password" = "L7bzd1gmm+db",
--     "jdbc_url" = "jdbc:mysql://10.63.8.125:33306",
    "jdbc_url" = "jdbc:mysql://10.63.8.125:33306",
    "driver_url" = "mysql-connector-java-8.0.25.jar",
    "driver_class" = "com.mysql.cj.jdbc.Driver"
);
