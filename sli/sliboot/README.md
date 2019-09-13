# Standalone

## Introduction
This is an example of running SLI outside of Karaf and without tight binding to ODL.

## Startup instructions
Start the application by running `mvn spring-boot:run -DserviceLogicDirectory=D:/dev/mygraphs -Dserver.port=9090`

The value of serviceLogicDirectory should be the path of directory containing the graphs to load. The graphs should be in XML format.

Once running requests can be submitted to `http://localhost:9090/executeGraph`

An example request
```
{
    "graphDetails": {
        "module": "kevin",
        "rpc": "test",
        "mode": "sync"
    },
    "input": {
        "name": "kevin",
        "test": "one",
        "mixed": "cAsE"
    }
}

```
An example response
```
{
    "input.test": "one",
    "hello": "world",
    "SvcLogic.status": "success",
    "input.mixed": "cAsE",
    "input.name": "kevin",
    "status": "success",
    "currentGraph": "SvcLogicGraph [module=kevin, rpc=test, mode=sync, version=1, md5sum=ec1f1b67439271d77fc9104e1723bcfd]"
}
```