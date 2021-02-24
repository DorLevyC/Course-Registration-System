//
// Created by spl211 on 03/01/2021.
//

#ifndef CLIENT_READFROMSOCKET_H
#define CLIENT_READFROMSOCKET_H
#include "ClientConnectionHandler.h"

class ReadFromSocket {
private:
    ClientConnectionHandler* clientConnectionHandler;

public:
    ReadFromSocket(ClientConnectionHandler & _clientConnectionHandler);
    void run() const;
};


#endif //CLIENT_READFROMSOCKET_H
