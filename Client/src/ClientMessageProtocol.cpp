
#include <iostream>
#include "../include/ClientMessageProtocol.h"

void ClientMessageProtocol::process(Message &msg) {
    short opCode = msg.response();

    if (opCode == 4 && msg.getReceiveOpCode() == 12){//successful LOGOUT message
        shouldTerminateClient = true;
        std::cout << "Thank you for using Course-Registration-System. Press any key to exit" << std::endl;
    }
    else {
        std::cout << "Please enter a command:" << std::endl;
    }

}

bool ClientMessageProtocol::shouldTerminate() const {
    return shouldTerminateClient;
}
