//
// Created by spl211 on 03/01/2021.
//

#ifndef CLIENT_READFROMKEYBOARD_H
#define CLIENT_READFROMKEYBOARD_H
#include "ClientConnectionHandler.h"

#include <string>

class ReadFromKeyboard {
private:
    ClientConnectionHandler * clientConnectionHandler;


public:
    ReadFromKeyboard(ClientConnectionHandler & _clientConnectionHandler);
    void run() const;
};


#endif //CLIENT_READFROMKEYBOARD_H
