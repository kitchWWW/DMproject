#!/bin/bash

rm kv-3.5.2
unzip kv-ee-3.5.2.zip
cd kv-3.5.2/lib/
while true; do
	java -jar kvstore.jar kvlite
	echo "failed, trying agian"
done
