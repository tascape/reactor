#!/bin/bash

export GPG_TTY=$(tty) && mvn clean deploy -Prelease
