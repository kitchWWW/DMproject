#!/bin/bash
set -e
NAM=''
CP='./:./slf4j-api-1.6.4.jar:./xml-apis.jar:./slf4j-log4j12-1.6.4:./kvclient.jar:./log4j-1.2.16.jar:./xercesImpl.jar:./xml-apis-1.4.01.jar:./slf4j-simple-1.7.13.jar:./slf4j-api-1.7.13.jar:./sdordfnosqlclient.jar:./xercesImpl-2.10.0.jar:./jena-iri-0.9.4.jar:./jena-core-2.7.4.jar:./jena-arq-2.9.4.jar:'
PRAM='kvstore localhost 5000 1'
NAM="Example$1"
echo "About to Compile $NAM"
javac -classpath ${CP} ${NAM}.java 
echo "About to run"
java -classpath ${CP} ${NAM} ${PRAM} > out.txt
echo "Done"