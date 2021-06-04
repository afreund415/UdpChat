Andreas Carlos Freund
acf2175
CSEE-4119 Computer Networks
Programming Assignment #1


Installing the project
    1. Unzip [filename] into its own directory 
    2. Make sure JRE and JDK are both installed on test machine
    3. In the directory from the commandline run: <make>

Running the project: 
    Note: The project depends on json.jar. There are multiple ways to run the project. 2 and 3 allow commandline arguments in the same line 
    1. <make run>  
    2. <java -jar UdpChat.jar> + (commandline args)
    3. <java -cp "./:./json.jar" UdpChat> + (commandline args)
    
CommandLine options: 
    Note: For local testing, the application can run in both client and server mode at the same time. For example, you can run <-s 3000 -c Andreas 127.0.0.1 3000 3001>. However, the client and server ports must be different. 
    Note: Usernames are case insensitive

    -s <port> (Starts/restarts the server)
    -c <nick-name> <server-ip> <server-port> <client-port> (starts a client)
    -q (shuts down the program)
    -quit (another command for shutting down program)
    -qs (shuts down the server)
    reg <nick-name> (registers a user or logs a user back in. 
        <nickname> is optional)
    dereg (deregisters a user)
    send <name> <message> (allows user to send message to online/offline users)
    -debug (toggles debug messages off and on in the code for testing)

Architecture: 
    -Classes that are implemented and what each one is doing 

    Each client and server has receive and send thread that picks up message from message queue 

    -Message class handles all message handling and Server and Client classes extend it for added functionality and interpretation. 
    -Mention NAT...both send and receive use same local port in order to allow clients to run behind NAT on private addresses. I tested this. The server saves the port and address for each client from the incoming registration message to account for NAT port translation


Message protocol: 
    All messages are sent in JSON format. Each message has a unique id for verification and data relability. Each message also has a date and receives an ACK message back with the same unique ID of the original message. The type of message is in the field "type". 

Client—>Server
{  
  "type": "reg",  
  "date": "04-06-2021 17:34:33",
  "addr": "127.0.0.1",
  "port": 3000,
  "name": "Dre",
  "id": "48b757e9-acc9-405f-ac1d-64a6fe45f22b"  
}

Client—>Server
{
  "type": "dereg", 
  "date": "04-06-2021 17:32:13",
  "addr": "127.0.0.1",
  "port": 3000,
  "name": "Dre",
  "id": "878e4046-fadf-4ec7-84fc-1f95642b9447"
}
Server—>Client
{
  "type": "regerror", 
  "date": "04-06-2021 17:54:58",
  "addr": "127.0.0.1",
  "port": 3002,
  "text": "John username is taken",
  "id": "4153deff-5f58-48c0-a498-ba4ff0b88899"
}

ACK 
{
  "port": 3000,
  "id": "cd347181-6c00-4743-8c3c-91b75577a6ed",
  "addr": "127.0.0.1",
  "type": "ack"
}

Server—> Client 
{
  "type": "table", 
  "date": "04-06-2021 17:41:25",
  "port": 3001,
  "name": "John",
  "online": true,
  "id": "a12d20d6-3b14-44b1-9bd1-87cd0e2a4d68",
  "addr": "127.0.0.1",
  "table": {
    "dre": {
      "port": 3001,
      "name": "Dre",
      "online": true,
      "addr": "127.0.0.1"
    },
    "john": {
      "port": 3002,
      "name": "John",
      "online": true,
      "addr": "127.0.0.1"
    }
  }
}

Client—>Client, Server–> Client, or Client —>Server
{
  "type": "chat", 
  "date": "04-06-2021 17:45:53",
  "addr": "127.0.0.1",
  "port": 3001,
  "name": "dre",
  "from": "John",
  "text": "This project was fun! ",
  "id": "b74c836f-d78d-4f86-8d70-9cb7406932f8"
}



Testing: 


1. UdpChat class 
    -creating server
    -creating client 
    -parsing commandline + args 
    
2. Server class (exends message class)
    
    A. Processing 
    - Creates and edits JSON table of clients
    - Registration 
    - Deregistration 
    - Offline chat 
     
    B. Broadcasting 
    - Broadcast client table 
    - Offline message broadcast 
    - ACKs



3. Client class (extends message class)

    A. Processing
    - JSON Client table
    - Incoming messages 
    
    B. Sending
    - Registration/Deregistration 
    - Messages 


4. Message class (UDP implementation here)
    A. Send thread

    B. Receive thread 

    C. ACK handling + error handling 

    Note: All messages are JSON objects


