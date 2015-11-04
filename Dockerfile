FROM java:latest

WORKDIR /prism
COPY target/prism-0.2.jar /prism/prism-0.2.jar
COPY target/dependencies /prism/dependencies


EXPOSE 8080

CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-cp", "prism-0.2.jar:dependencies/*", "com.ant.crawler.core.MainExecutor"]