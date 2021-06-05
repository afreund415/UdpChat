Andreas Carlos Freund
acf2175
CSEE-4119 Computer Networks
Programming Assignment #1

Hightlights: 
    1. Multi-threaded implementation for both send and receive
    2. Client works behind NAT
    3. Messages and internal representations are all JSON format
    4. Server and client can run in the same instance
    5. All commands are available from the command line or app prompt
    6. Debug mode

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
    Note: For local testing, the application can run in both client and server mode at the same time. For example, you can: 
    run <-s 3000 -c Andreas 127.0.0.1 3000 3001>. 
    However, the client and server ports must be different. 
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
    My project utilizes 4 main classs. UdpChat, Message, Client and Server. The UdpChat class compiles the entire project and handles command line arguments. The Message class handles sending and receiving of messages and is where all the UDP socket handling lives. The Server and Client classes extend the message class, construct Server and Clients, and handle their specific implementations of the Message class. The Message class utilizes multithreading for Receiving and Sending, and all messages are in JSON format. 

    UdpChat:
    The UdpChat class contains the project's Main function and handles all command line arguments. The class also has a method called argParse that uses Switch case statements to handle the different commands, such as constructing new Clients and starting the Server, sending a message, and exiting the program. It also contains a debug toggle command to assist with debugging the project. The class also has a validateName helper method for ensuring usernames only utilize alphanumeric characters.   

    Message class: 
    The message class uses multithreading for sending and receiving all messages, which are all in JSON format. Both the Send and Receive threads use the same local port in order to allow clients to run behind NAT on private addresses. The server saves the port and address from each client from incoming registration message to account for NAT port translation (in the server class). The Send and Receive threads handle all the UDP socket implementation (aside from ACK sending). The class also contains a sendMessage method that constructs messages as JSON objects, with a unique ID for ACK verification, a date, IP address, port, and type. This method then places the message in a messageQueue for the receive thread to handle. Additionally, the class has a sendACK method that bypasses the timeouts imposed on the normal Send thread, as well as findUser methods for locating users, and 3 abstract methods for overloading in the extended Server and Client classes. Those methods are recvACK (for ACK handling), recvMsg (for receiving messages), and sendFail (for handling sending failures). The class has a findMessage method for locating messages by unique ID, a stopMessages method for clean shut down of the threads, and a sleepMs method for inducing a timedelay on our threads when needed to ensure synchronization. Lastly, the class has several print methods for printing system messages, errors, and debug statements.      

    Server class: 
    This class extends the Message class, contains a Server constructor, implementation of the abstract Message class methods sendFail, recvMsg, and recvACK, amongst other methods. The recvMsg is the largest method in the class and handles the various types of messages the server receives and sends. This method handles all client registration and deregistration requests as well as offline chat. The sendFail implementation sets users to offline when a message is not received and invokes the offline chat flow. The storeChatOffline method creates a JSONArray that stores messages (JSONObjects) that are received by an offline user. When a user registers, the server automatically checks if they have offline messages waiting. The server also contains a updateClients method that maintains a clientTable of all clients, their online status, and contact information (port number and IP address). The method broadcasts the updated table to each user whenever a user registers or deregisters. 

    Client class: 
    The client class extends the Message class, contains a Client constructor, implementation of the abstract Message class methods sendFail, recvMsg, and recvACK, amongst other methods. The recvMsg implementation uses Switch cases to account for the various types of messages Clients will interact with. For table messages, messages that broadcast the updated clientTable that each client maintains a local copy of, alerts users to changes in online status for all users. The chat case simply prints out messages received by the client, and the registration error case prints messages when the server detects an error in registration. The recvACK method deals with the various types of ACKs clients will get and takes appropriate action, for instance when an offline chat is sent and received. The sendfail implementation alerts users to issues in sending attempts and exits in the case of the server going offline prematurely. Lastly, this class has a method for stopping its send and receive threads when the user exits.   


Message protocol: 
    All messages are sent in JSON format. Each message has a unique id for verification and data relability. Each message also has a date and receives an ACK message back with the same unique ID of the original message. The type of message is in the field "type". Examples below:


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


Tests
1. Checking launch modes
    
    #java -jar UdpChat.jar -s 3000
    >>> -c Andreas 127.0.0.1 3000 4000
    >>> Welcome Andreas! You are registered
    >>> -q
2. Checking launch mode with CL args
    
    # java -jar UdpChat.jar -s 3000 -c Andreas 127.0.0.1 3000 4000
    >>> Welcome Andreas! You are registered
    >>> -q
3. Checking class-path launch mode and running server and client same time
    and chatting myself

    # java -cp "./:./json.jar" UdpChat -s 3000 -c Andreas 127.0.0.1 3000 4000
    >>> Welcome Andreas! You are registered
    >>> send Andreas does this work
    >>> 05-06-2021 00:54:32 - Andreas: does this work
    >>> Message received by andreas
    >>> -q

4. 3 way chat with reg, dereg, offline chat, server quitting before user    
   exit + offline chat, self message, reg with duplicate username, normal reg after dereg on client instance
   
   Server: 
   # make run
   >>> -s 3000
   >>> -qs
   Server stopped ***(part marked with asterisk in instance 2 corresponds to    
                      same time)***

   Client instance 1: 
   $ make run
    >>> -c John 127.0.0.1 3000 4000
    >>> Welcome John! You are registered
    >>> Mary is online
    >>> Vic is online
    >>> Send Vic Hello
    >>> Message received by vic
    >>> Send John hello
    >>> 05-06-2021 01:22:54 - John: hello
    >>> Message received by john
    >>> send mary hello
    >>> Message received by mary    
    >>> 05-06-2021 01:23:45 - Mary: hello 
    >>> 05-06-2021 01:23:57 - Vic: hi!
    >>> Mary is offline
    >>> Send mary we miss you! Come back! 
    >>> Message received and saved by server
    >>> Mary is online
    >>> Andreas is online
    >>> dereg
    Attempting deregistration for John
    >>> Deregistration complete
    >>> You are Offline. Bye.


   Client instance 2: 
   $ make run 
   >>> -c Vic 127.0.0.1 3000 3005
   >>> Welcome Vic! You are registered
   >>> John is online
   >>> Mary is online
   >>> 05-06-2021 01:20:51 - John: Hello
   >>> 05-06-2021 01:23:42 - Mary: hello 
   >>> send mary hello 
   >>> Message received by mary
   >>> send john hi!  
   >>> Message received by john
   >>> Mary is offline
   >>> Send mary come back! 
   >>> Message received and saved by server
   >>> Mary is online
   >>> Mary is offline
   >>> Andreas is online
   >>> John is offline
   >>> Send John you here? ***(sent after server stopped)***
   >>> No ACK received after timeout and 5 retries
   >>> Server not responding
   >>> Exiting


   Client instance 3: 
   $ make run
   >>> -c Mary 127.0.0.1 3000 4500
   >>> Welcome Mary! You are registered
   >>> Vic is online
   >>> John is online
   >>> 05-06-2021 01:23:13 - John: hello
   >>> send vic hello 
   >>> Message received by vic
   >>> send john hello 
   >>> Message received by john
   >>> 05-06-2021 01:23:53 - Vic: hello 
   >>> dereg
   Attempting deregistration for Mary
   >> Deregistration complete
   >>> You are Offline. Bye.
   >>> Welcome Mary! You are registered
   >>> Vic is online
   >>> John is online
   >>> 05-06-2021 01:28:02 - Chat Server: Welcome back! You got mail.
   >>> 05-06-2021 01:28:02 - John: we miss you! Come back! 
   >>> 05-06-2021 01:28:02 - Vic: come back! 
   >>> dereg
   Attempting deregistration for Mary
   >>> Deregistration complete
   >>> You are Offline. Bye.
   >>> dereg
   >>> reg Vic
   >>> Vic username is taken
   >>> reg Andreas
   >>> Welcome Andreas! You are registered
   >>> Vic is online
   >>> Mary is offline
   >>> John is online
   >>> John is offline





   

   

    

