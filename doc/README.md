### case development environment

* first time setup, run the following line in console

```
cd && mkdir -p ~/qa/th && wget https://raw.githubusercontent.com/tascape/reactor/master/doc/th.sh -O th.sh && bash th.sh
```


* restart environment, after host machine reboot

```
cd && vagrant up
```


* destroy environment (for re-setup)

```
cd && vagrant destroy
```


(tested on OS X Yosemite 10.10.5, with Vagrant 1.7.4 and VirtualBox 5.0.2)  
