# SimpleMail

Rahul Kejriwal, CS14B023

A Simple Mail service client and server program using TCP sockets

## Instructions(Server): 
   
1. Build the class files using:
    make server
2. Change directory to bin:
    cd MAILSERVER
3. Run server using:
    java server <PortNo>
    You can use port no. 1080.  

## Instructions(Client): 

1. Build the class files using:
    make client
2. Change directory to bin:
    cd MAILCLIENT
3. Run server using:
    java client <hostname/ip> <PortNo>
        
Note: hostname/ip is either the hostname of the server machine (should be present in the /etc/hosts file) or the IP address of the server machine.

Note: You can use port no. 1080 (should agree with server).
