Prerequsites:
(this is just a sample setup, you can try other configurations)

1. Development machine - Mac OSX, with java 8, and IDE (Netbeans/Eclipse/...).
2. Deployment server(Linux), with java 8, apache tomcat 8, mysql 5.
3. Test machine (reuse of deployment server), with java 8, firefox and Xvfb (for web UI testing), apache httpd (for serving test log)


There are 2 sub-directories: th and thr.

1. th - all source code, build dependencies and build scripts for test automation framework.
2. thr - all source code, build/deploy dependencies and build scripts for test reporting

