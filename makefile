all: server client

server: code/cmdHandlers.c
	gcc code/server.c -g -o bin/server

client:
	gcc code/client.c -g -o bin/client

rs:
	@./bin/server