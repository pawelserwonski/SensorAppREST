# Sensors

Java + Spring Boot + Spring MVC + H2 + Tomcat + Maven

## Task Content
We have defined list of sensors installed in variety of engines. Every engine has exactly one pressure sensor and at least one temperature sensor. The task is to write web service with HTTP/REST interface, which will enable to update current value of sensors and on demand will return the list of incorrectly working engines.

Server should download data containing list of sensors in YAML-format file from repository placed on GitHub server. Example of list:

```
- id: "2567"
  engine: "123"
  type: "pressure"
  name: "pressure 123"
  value: 2004
  min_value: 1500
  max_value: 5000
- id: "78946"
  master-sensor-id: "2567"
  type: "temperature"
  value: 101
  min_value: 0
  max_value: 350
```

Where:
* 'id' - id number of sensor
* 'type' - info about type of sensor (allowed values are *pressure* and *temperature*)
* 'engine' - id of engine assigned to pressure sensor
* 'master-sensor-id' - id of temperature sensor assigned to parent pressure sensor
* 'value' - current value of measured units
* 'min_value' - minimal value which can be measured by the sensor
* 'max_value' - maximum value which can be measured by the sensor

Note: As we can see in the example, list has no beginning header and every object can have not only required values, but also additional, meaningless ones.

## Requirements

HTTP server should get URL to input file as an argument, when it is starting in command line, for example:
```
./server --config=https://github.com/pawelserwonski/SensorAppREST/blob/master/sensors/sensorList.yml
```

Server should enable to execute following HTTP queries:

1. Getting list of incorrectly working engines.
```
curl -XGET "http://localhost:8080/engines?pressure_threshold=40&temp_threshold=50"
```

where:
* pressure_threshold - value of pressure **below** which we consider engine as working incorrectly
* temp_threshold - value of temperature **above** which we consider engine as working incorrectly

Result returned in response in HTTP body should contain list of engines, which has value of pressure below *pressure_threshold* **and also** value of temperature at least on one sensor above *temperature_threshold*. Result should have list of engines IDs in JSON format.

2. Updating sensor current value.
```
curl -XPOST "http://localhost:8080/sensors/89145" -H "Content-Type: application/json: -d '{"operation": "increment", "value": "5"}'
```

Possible values for *operation* field are:
* set - setting value 
* increment - incrementing value
* decrement - decrementing value

It must be ensured that values will not go beyond defined min and max values.

## Additional task
Write an instruction how to compile the code, run tests and run the solution. Everything should be in INSTRUCTION.md file.

## TODO
Although app is working, it still needs some improvements. Next I'm going to focus on:
* GithubYamlParser - improving retrieving raw file from Github server (especially replacing current method of preparing URL to raw file)
* Whole app - add coments
* Whole app - set up project to compile also as Docker container
