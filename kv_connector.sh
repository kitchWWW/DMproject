#!/bin/bash

cd kv-3.5.2/lib/
while true; do
	java -jar kvstore.jar kvlite
	echo "failed, trying agian"
done
