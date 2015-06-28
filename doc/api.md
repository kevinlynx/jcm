
## cluster

### create

`/c` `POST`

    {"name":"search","nodes":[{"ip":"119.75.218.70","protos":[{"port":80,"proto":"HTTP"}],"udata":"" }] ,"checkType":"HTTP", "checkFile":"/"}

### delete

`/c/{name}` `DELETE`

### get

`/c/{name}` `GET`

### get all

`/c` `GET`

### online/offline

`/c/online/{name}` `POST`

    true/false

### add nodes

`/c/node/{name}` `POST`

    [{"ip":"119.75.218.70","protos":[{"port":80,"proto":"HTTP"}],"udata":"" }, {"ip":"127.0.0.1", "protos":[{"port":8080,"proto":"HTTP"}]}]

### delete nodes

`/c/node/{name}` `DELETE`

    ["119.75.218.70|http:80", "127.0.0.1|http:8080"]

### set node online/offline

`/c/node/online/{name}` `POST`

    {"spec":"127.0.0.1|http:8000", "online":true}

## sys

### leader

`/sys/leader `GET`

### check list

`/sys/checklist` `GET`

## subscribe

`/sub` `POST`

    {"tag":"v0.1.0", "units":[{"name":"hello9", "version":0}]}

    
