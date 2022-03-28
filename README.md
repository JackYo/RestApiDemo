# REST API Demo
It is implemented with Java Spring boot

# Environment requirement
* JRE 11 or higer version

# How to run
1. download *restApiDemo.jar*
2. run following command
```
java -jar demo.jar
```
* It will automatically create a `uploads` folder and store files there.

# Run with docker
* If you want to run with Docker, install `Docker Engine` and `Docker Compose` first.
  In Windows and MAC, there is `Docker Desktop` to make things visual.
1. Before starting, make sure you CLI is in the root folder of this project.
2. Use following command to build docker image, at the mean time you are building java program.
```
docker build -t restdemo .
```
3. Then you can run generated image with Docker Compose.
```
docker-compose up -d
```
