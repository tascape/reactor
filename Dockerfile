FROM maven:3-alpine

RUN apk update && apk add iputils
