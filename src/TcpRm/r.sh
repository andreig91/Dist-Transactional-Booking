#!/bin/bash
cd $HOME/rm/src/TcpRm
rm -rf *.class
javac server.java
java server $1
