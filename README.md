# file-server
Multi client server application for saving and retreiving files

Application for client requests (PUT, GET; DELETE) using sockets. 
The client can save, retreive and delete files from the server. When a client
types in the request 'exit', the whole server shuts down. 

The client can choose to search for a file on the server by the file name, or a file ID.
The HashMap that keeps track of the files by the file ID in the server filesystem, gets serialized 
to its own special file 'data.ser' every time the server shuts down, and gets deserialized when
the server starts back up again. 
