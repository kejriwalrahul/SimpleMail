all: server client

server: src/server.java
	javac -d bin/ src/server.java

client: src/client.java
	javac -d bin/ src/client.java

rs: server
	@cd bin && java server 1080

rc: client
	@cd bin && java client localhost 1080

clean:
	rm bin/*
