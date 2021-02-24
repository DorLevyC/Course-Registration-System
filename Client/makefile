CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lboost_thread -pthread

all: BGRSclient
	g++ -o bin/BGRSclient bin/ClientConnectionHandler.o bin/BGRSclient.o bin/MessageDecoder.o bin/ClientMessageProtocol.o bin/Message.o bin/ReadFromKeyboard.o bin/ReadFromSocket.o $(LDFLAGS)

BGRSclient: bin/ClientConnectionHandler.o bin/BGRSclient.o bin/MessageDecoder.o bin/ClientMessageProtocol.o bin/Message.o bin/ReadFromKeyboard.o bin/ReadFromSocket.o
	
bin/ClientConnectionHandler.o: src/ClientConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ClientConnectionHandler.o src/ClientConnectionHandler.cpp

bin/BGRSclient.o: src/BGRSclient.cpp
	g++ $(CFLAGS) -o bin/BGRSclient.o src/BGRSclient.cpp

bin/MessageDecoder.o: src/MessageDecoder.cpp
	g++ $(CFLAGS) -o bin/MessageDecoder.o src/MessageDecoder.cpp

bin/ClientMessageProtocol.o: src/ClientMessageProtocol.cpp
	g++ $(CFLAGS) -o bin/ClientMessageProtocol.o src/ClientMessageProtocol.cpp

bin/Message.o: src/Message.cpp
	g++ $(CFLAGS) -o bin/Message.o src/Message.cpp

bin/ReadFromKeyboard.o: src/ReadFromKeyboard.cpp
	g++ $(CFLAGS) -o bin/ReadFromKeyboard.o src/ReadFromKeyboard.cpp

bin/ReadFromSocket.o: src/ReadFromSocket.cpp
	g++ $(CFLAGS) -o bin/ReadFromSocket.o src/ReadFromSocket.cpp

.PHONY: clean
clean:
	rm -f bin/*
