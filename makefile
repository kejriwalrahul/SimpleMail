all: server client

server: src/server.java
	javac src/server.java

client: src/client.java
	javac src/client.java

rs: server
	@cd src && java server 1080

rc: client
	@cd src && java client localhost 1080