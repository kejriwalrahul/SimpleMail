/*
	Code by Rahul Kejriwal, CS14B023
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// For listing directory files
#include "cmdHandlers.c"

/*
	Command Processor

	Commands supported:
		LSTU				List users
		ADDU <userid>		Adds user
		USER <userid>		Sets current user and returns stats
		READM				Send current mail
		DELM				Delete current mail
		SEND <userid>		Sends msg
		DONEU				Close current user
		QUIT				Close connection
*/

/*
	Dispatches command to relevant handler.
*/
char* dispatch_cmd(char* cmd){
	char *token, *arg;
	token = strtok(cmd, " ");
	arg	  = strtok(NULL, " ");

	if(!strcmp(token, "LSTU"))
		return listUsers();
/*	else if(!strcmp(token, "ADDU"))
		return addUser(arg);
	else if(!strcmp(token, "USER"))
		return setCurrUser(arg);
	else if(!strcmp(token, "READM"))
		return readCurrMsg();
	else if(!strcmp(token, "DELM"))
		return delCurrMsg();
	else if(!strcmp(token, "SEND"))
		return sendMsg(arg);
	else if(!strcmp(token, "DONEU"))
		return closeUser();
	else if(!strcmp(token, "QUIT"))
		return closeConnection();*/
	else
		errorExit("Can't understand client command!");
}

int main(){

	printf("%s\n", listUsers());
	return 0;
}