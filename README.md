# polyglotify-assessment-service

## prerequisite 
- java 17 
- maven
- docker desktop

## setting up docker image
- docker compose command- docker compose up
- ensure that conatainer for postgresql and redis is up and running
- browse http://localhost:8282/ on browser
- enter userid as admin@admin.com, and password as admin
- connect to postgresql database running inside container
- host name should be db , userid as admin , password as admin
- once connected to assessment db , run the DDL and DML scripts inside sqlscript.sql

