#!/bin/sh
export ZK=<zk-path>
gradle jar
java -Dlog4j.configuration=file:$ZK/conf/log4j.properties -jar build/libs/MiniTrello.jar