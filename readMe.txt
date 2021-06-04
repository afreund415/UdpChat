Andreas Carlos Freund
acf2175


Structure: 
    - Program with 4 major files, use of JSON, and multithreading
    - Classes: UdpChat, Message[Send thread class and Receive thread class], Server, and Client
    - 

CommandLine instructions: 
    1. 
    
CommandLine cases: 
    -c 
    -s
    -q
    -

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


