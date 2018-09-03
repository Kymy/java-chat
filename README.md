# javachat


## Install components

Run ` mvn install ` to download and install needed components

## Project compiling

Run ` mvn compile ` to compile the source code

## Server

Run `mvn exec:java -Dexec.mainClass="mx.unam.ciencias.myp.server.Server" -Dexec.args="3001"` to run the server on port 3001

## Chat. Can run multiple clients at the same time

Run `mvn exec:java -Dexec.mainClass="mx.unam.ciencias.myp.chat.Chat" -Dexec.args="localhost 3001"` to run the chat

## To identify users

Run `IDENTIFY username` to run the client

## To set status to users userstatus = {ACTIVE, AWAY, BUSY}

Run `STATUS userstatus` 

## To show all identified users

Run `USERS` 

## To send message to identified user 

Run `MESSAGE username messageContent`

## To send message to all identified users

Run `PUBLICMESSAGE messageContent`

## To disconnect 

Run `DISCONENCT`
