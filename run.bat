#set JAVA_HOME to JRE 1.6 (not support JRE 1.7)
set JAVA_HOME="C:\Program Files\Java\jre6"
set JAVA_HEAP_MAX=-Xmx1g
set CRAWLER_LIB=prism-0.0.1-SNAPSHOT.jar:lib/*
%JAVA_HOME%\bin\java %JAVA_HEAP_MAX% -Dfile.encoding=UTF-8 -cp %CRAWLER_LIB% com.ant.crawler.core.MainExecutor