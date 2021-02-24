#include <iostream>
#include "../include/ReadFromKeyboard.h"

void ReadFromKeyboard::run() const{
    bool result = true;
    while (result) {
        const short bufsize = 1024;
        char buf[bufsize];

        //get command from the client's keyboard
        std::cin.getline(buf, bufsize);
        std::string message(buf);
        //pass the message to the connection handler
        result = clientConnectionHandler->sendMessage(message);
    }
}

ReadFromKeyboard::ReadFromKeyboard(ClientConnectionHandler & _clientConnectionHandler):clientConnectionHandler(& _clientConnectionHandler) {}
