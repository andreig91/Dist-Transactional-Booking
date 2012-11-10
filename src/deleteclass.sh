#!/bin/bash

cd $HOME/workspace/rm/src/TcpServer
rm -f *.class
cd ../LockManager 
rm -f *.class
cd ..
javac TcpServer/RmServer.java
javac TcpServer/MwServer.java
javac TcpServer/client.java
