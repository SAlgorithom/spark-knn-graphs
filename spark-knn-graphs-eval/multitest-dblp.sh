#!/bin/bash

CLASS="info.devatty.spark.knngraphs.eval.MultiDBLP"

### Local test
JAR="target/spark-knn-graphs-eval-0.1-SNAPSHOT.jar"
SPARK="/opt/spark-1.6.2-bin-hadoop2.6/bin/spark-submit"
OPTS=""
DATASET="../../../Datasets/dblp/dblp.xml"

### Eurecom cluster
#JAR="spark-knn-graphs-eval-0.1-SNAPSHOT.jar"
#SPARK="/home/ubuntu/usr/spark-1.5.1-dist/bin/spark-submit"
#OPTS="--driver-memory 8g --num-executors 16 --executor-cores 4 --executor-memory 4g --master yarn --deploy-mode client --conf spark.yarn.executor.memoryOverhead=1024 --conf spark.rpc.askTimeout=300s"
#DATASET="../datasets/dblp/dblp.xml"

for i in {1..1}
do
  echo "Test $i"
  $SPARK $OPTS --class $CLASS $JAR $DATASET
  
  #rm /tmp/spark-events/*
  
  # Send e-mail when done
  #../sendmail.py "MultiTest with DBLP dataset has completed iteration $i"
done

