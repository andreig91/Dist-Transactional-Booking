#!/bin/bash
cd $HOME/rm/src/TcpServer
rm -rf *.class
javac server.java
java server $1 willy $2 skinner $3 linux $4

