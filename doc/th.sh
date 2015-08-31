#!/bin/bash

 
if (type vagrant) && (type virtualbox) then

    echo "get latest vagrant file"
    wget https://raw.githubusercontent.com/tascape/testharness/master/doc/Vagrantfile -O Vagrantfile
    vagrant up


    mkdir -p $HOME/.th
    mkdir -p $HOME/qa/th

    export PROP=$HOME/.th/th.properties
    echo "create testharness system properties file" $PROP
    echo "# testharness system properties" > $PROP
    echo "# use -Dkey=value to override or add in commandline " >> $PROP
    echo "qa.th.db.type=mysql" >> $PROP
    echo "qa.th.db.host=localhost:13306" >> $PROP
    echo "qa.th.log.path=$HOME/qa/th/logs" >> $PROP
    cat $PROP

    echo "check report at http://localhost:18088/thr/suites_result.xhtml"
    open "http://localhost:18080/" || echo "OK"
    open "http://localhost:18088/thr/suites_result.xhtml" || echo "OK"

else
    echo "you need to first install vagrant (http://www.vagrantup.com/downloads.html) and virtualbox (https://www.virtualbox.org/wiki/Downloads)"
fi
