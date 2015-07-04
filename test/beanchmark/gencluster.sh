#!/bin/sh

c_cnt=1000
node_cnt=500
mkdir tmp

generate_cluster()
{
    c=$1
    dst=tmp/$c.json
    cp template.json $dst
    node="{\"ip\":\"10.181.97.120\",\"protos\":[{\"port\":20009,\"proto\":\"HTTP\"}, {\"port\":3333,\"proto\":\"TCP\"}], \"online\":\"ONLINE\" }"
    sed -i "2 i$node" $dst
    for((i=1; i < $node_cnt; ++i))
    do
        p=$(($i + 3333))
        node="{\"ip\":\"10.181.97.120\",\"protos\":[{\"port\":20009,\"proto\":\"HTTP\"}, {\"port\":$p,\"proto\":\"TCP\"}], \"online\":\"ONLINE\" },"

        sed -i "2 i$node" $dst
    done
    sed -i "s/cluster/$c/g" $dst
}

for((j=0; j < $c_cnt; ++j))
do
    generate_cluster "cluster$j"
done

