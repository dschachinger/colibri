@echo off

set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_60"
echo %JAVA_HOME%

echo 'Deploy started'
deploytool -package matlaboptimizer.prj
echo 'Deploy finished'
