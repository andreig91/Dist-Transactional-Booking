#!/bin/bash

cd $HOME/rm/src/TcpServer
rm -f *.class
cd ../LockManager 
rm -f *.class
cd ..
javac TcpServer/RmServer.java
javac TcpServer/MwServer.java
javac TcpServer/clientC.java
javac TcpServer/client.java
