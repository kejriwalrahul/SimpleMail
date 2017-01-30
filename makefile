all: server client

server: code/cmdHandlers.c
	@mkdir -p bin
	gcc code/server.c -g -o bin/server

client:
	@mkdir -p bin
	gcc code/client.c -g -o bin/client

rs:
	@./bin/server