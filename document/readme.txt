Prerequsites:
(this is just a sample setup, you can try other configurations, assuming your login user is johnd, you will run testharness under this name)

1. Development machine - Mac OSX, with java 8, and IDE (Netbeans/Eclipse/...).
2. Deployment server(Linux), with java 8, apache tomcat 8, mysql 5.
3. Test machine (reuse of deployment server), with java 8, firefox and Xvfb (for web UI testing), apache httpd (for serving test log)


Install/configure apache httpd on test machine (ubuntu)

1. sudo apt-get install apache2
2. sudo mkdir /var/www/logs
3. sudo chown johnd /var/www/logs
4. sudo mkdir /qa
5. sudo chown johnd /qa
6. ln -s /var/www/logs /qa/logs


Install Xvfb on test machine (ubuntu) for web UI testing

1. sudo apt-get install xvfb


Configure firefox on test machine for page loading performance testing

1. install plugin firebug
2. install firebug extension netexport


There are 3 sub-directories: th and thr.

1. th - all source code, build dependencies and build scripts for test automation framework.
2. thr - all source code, build/deploy dependencies and build scripts for test reporting
3. sample - sample usage of this automation framework.

