This directory contains a demo springboot implementation of the SLI-API healthcheck method.

To start this server, run:
mvn -DserviceLogicDirectory=src/main/resources spring-boot:run

This will start a servlet on port 8080.  To test to that servlet, post a blank
message to that port:

curl http://127.0.0.1:8080/restconf/operations/SLI-API:healthcheck -X POST -H "Content-Type: application/json"

Requests can also be sent to Once running requests can be submitted to `http://localhost:8080/executeGraph`

The graph details need to match a graph which has been loaded and activated

An example request
```
{
    "graphDetails": {
        "module": "prov",
        "rpc": "test",
        "mode": "sync"
    },
    "input": {
        "name": "Hello World",
        "test": "one",
        "mixed": "cAsE"
    }
}
```