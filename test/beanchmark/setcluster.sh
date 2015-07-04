#!/bin/sh

c_cnt=80

for((j=0; j < $c_cnt; ++j))
do
    file=tmp/cluster$j.json
    curl -i -X POST http://10.181.97.106:8080/c -H "Content-Type:application/json" --data-binary @$file
done
