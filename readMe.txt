Andreas Carlos Freund
acf2175


Structure: 
    - Single file with 4 major classes, use of JSON, and threads
    - ClientSend, ClientReceive, ServerSend, ServerReceive 
        (names subject to change)
    - helper methods for registration 
    - json for storage


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

    Notes: All messages are JSON objects

next tasks
1. create message as jason object     




1. Register
2. Chat 
3. Offline chat 


msgTable{
    table{
        username1: {name, addr, port, status}
        username2: {name, addr, port, status}
        username3: {name, addr, port, status}
        
    }
}


1. Send messages (commandline)
2. Send messages to other clients 
3. Storing offline messages on server


Git token: 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