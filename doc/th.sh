#!/bin/bash

 
if (type vagrant) && (type virtualbox) then

    echo "get latest vagrant file"
    wget https://raw.githubusercontent.com/tascape/testharness/master/doc/Vagrantfile -O Vagrantfile
    vagrant up


    mkdir -p $HOME/qa/th

    export PROP=$HOME/qa/th/th.properties
    echo "# testharness system properties" > $PROP
    echo "# user -Dkey=value to override in commandline " >> $PROP
    echo "qa.th.db.type=mysql" >> $PROP
    echo "qa.th.db.host=localhost:13306" >> $PROP
    echo "qa.th.log.path=$HOME/qa/th/logs" >> $PROP
    cat $PROP

    echo "run testharness with -Dqa.th.conf.file=$PROP"
    echo "check report at http://localhost:18088/thr/suites_result.xhtml"
    open "http://localhost:18088/thr/suites_result.xhtml" || echo "OK"

else
    echo "you need to first install vagrant (http://www.vagrantup.com/downloads.html) and virtualbox (https://www.virtualbox.org/wiki/Downloads)"
fi
