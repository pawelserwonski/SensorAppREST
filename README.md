# Sensor App
Paweł Serwoński - Relayr Recruitment Task

## Requirements

Software requirements to compile this application:
* JDK version 8 (1.8) or newer
* Maven

To install this software on Debian/Ubuntu type below lines in terminal:

```
sudo apt-get update
sudo apt-get install default-jre
sudo apt-get install default-jdk
sudo apt-get install maven
```

To install JDK and Maven in Microsoft Windows please go to this websites:
* http://www.oracle.com/technetwork/java/javase/downloads/index.html
* https://maven.apache.org/download.

In Microsoft Windows to use Maven as it is shown in this insctruction you need to add mvn.cmd to path. Guide how to do it:
* https://maven.apache.org/guides/getting-started/windows-prerequisites.html
* https://www.mkyong.com/maven/how-to-install-maven-in-windows/

## Compile

To compile this app you need to open terminal and go directory containing **pom.xml** file and **src** folder. You can to it by typing:
```
cd <path_to_directory>
```

You can also open folder in Explorer, right click (in Windows please hold Shift while right-clicking) and choose `Open terminal here` (in Windows terminal app is called `PowerShell`).

Having the terminal opened in the directory this will compile app, run tests and create *.war file:
```
mvn clean package
```

To run only unit tests type:
```
mvn test
```

Effect of compiling and testing will be shown in terminal. Compiled files will be in `target` folder.
## Running the application

To run application you need to open `target` directory in terminal. Type in terminal:
```
cd target
```

To run application type below code in terminal:
```
java -jar SensorApp-1.0-SNAPSHOT.war --config=<url_to_github_yml_file>
```

Where `<url_to_github_yml_file>` is a link to file placed in Github repository containing list of sensors. (Example of file: https://github.com/relayr/pdm-test/blob/master/sensors.yml)

Note: --config parameter is not necessary however ommiting it will start application with empty repository. It is option recommended only to run the tests.