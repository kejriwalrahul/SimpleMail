all: server client

server: cmdHandlers.c
	gcc server.c -g -o server

client:
	gcc client.c -g -o client
