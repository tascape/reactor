### Test development environment

1. first time setup, run the following line in console

```
cd && wget https://raw.githubusercontent.com/tascape/testharness/master/doc/th.sh -O th.sh && bash th.sh
```


2. after host machine reboot, run the following in console

```
cd && vagrant up
```


3. destroy environment (for re-setup)

```
cd && vagrant destroy
```


(tested on OS X Yosemite 10.10.5, with Vagrant 1.7.4 and VirtualBox 5.0.2)  
