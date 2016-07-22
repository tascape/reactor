#!/bin/bash

 
if (type vagrant) && (type virtualbox) then

    echo "get latest vagrant file"
    wget https://raw.githubusercontent.com/tascape/reactor/master/doc/Vagrantfile -O Vagrantfile
    vagrant up

    mkdir -p $HOME/.reactor
    mkdir -p $HOME/reactor

    export PROP=$HOME/.reactor/reactor.properties
    echo "create reactor system properties file" $PROP
    echo "# reactor system properties" > $PROP
    echo "# use -Dkey=value to override or add in commandline " >> $PROP
    echo "reactor.db.type=mysql" >> $PROP
    echo "reactor.db.host=localhost:13306" >> $PROP
    echo "reactor.log.path=$HOME/reactor/logs" >> $PROP
    echo "reactor.JOB_NAME=local-run" >> $PROP
    echo "reactor.test.station=localhost" >> $PROP
    cat $PROP

    echo "check report at http://localhost:18088/reactor-report/suites_result.xhtml"
    open "http://localhost:18088/reactor-report/suites_result.xhtml" || echo "OK"

else
    echo "you need to first install vagrant (http://www.vagrantup.com/downloads.html) and virtualbox (https://www.virtualbox.org/wiki/Downloads)"
fi
