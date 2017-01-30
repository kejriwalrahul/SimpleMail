/*
	Code by Rahul Kejriwal, CS14B023
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// For listing directory files
#include <dirent.h>

// For storing current user's spool file
FILE *currfile;


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
	Prints an error msg, and terminates.
*/
void errorExit(char *err){
	printf("\nError: %s\n", err);
	exit(1);
}

/*
	Returns space separated user list.
*/
char* listUsers(){
	DIR *d;
	struct dirent *dir;

	char *userlist = malloc(2048);

	d = opendir(".");
	if(d){
		char *p;

		while( (dir = readdir(d)) != NULL){
			if( (p =strstr(dir->d_name, ".dat")) != NULL){
				strcat(userlist, strtok(dir->d_name, "."));
				strcat(userlist, " ");
			}			
		}

		return userlist;
	}
	else
		errorExit("Can't seem to open current directory!");
}

char* addUser(char*);
char* setCurrUser(char*);
char* readCurrMsg();
char* delCurrMsg();
char* sendMsg(char*);
char* closeUser();
char* closeConnection();