
#ifndef CLIENT_CLIENTCONNECTIONHANDLER_H
#define CLIENT_CLIENTCONNECTIONHANDLER_H


#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include "MessageDecoder.h"
#include "ClientMessageProtocol.h"

using namespace std;
using boost::asio::ip::tcp;

class ClientConnectionHandler {
private:
    bool invalidCommand();
    bool isValidInt(std::string & data);
    bool isValidString(int numOfStrings, std::string & data);
    const std::string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;
    MessageDecoder messageDecoder;
    ClientMessageProtocol clientMessageProtocol;
    bool shouldTerminate;

public:
    ClientConnectionHandler(std::string host, short port);
    virtual ~ClientConnectionHandler();

    // Connect to the remote machine
    bool connect();

    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);

    // Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);

    bool getMessage();
    bool sendMessage(string & msg);

    // Close down the connection properly.
    void close();

    void shortToChars(short num, char *arr, int begin);

}; //class ConnectionHandler


#endif //CLIENT_CLIENTCONNECTIONHANDLER_H
