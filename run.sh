#!/bin/bash

mvn clean package && java -jar target/mimir-1.0.0.jar
pause
