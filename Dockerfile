FROM maven:3.6.3-openjdk-14-slim

RUN apt update && apt install -y iputils-ping
