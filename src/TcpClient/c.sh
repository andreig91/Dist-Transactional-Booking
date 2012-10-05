#!/bin/bash
cd $HOME/rm/src/TcpClient
rm -rf *.class
javac client2.java
java client2 mimi $1
