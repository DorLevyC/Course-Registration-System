
#include "../include/ReadFromSocket.h"


ReadFromSocket::ReadFromSocket(ClientConnectionHandler & _clientConnectionHandler):
clientConnectionHandler(&_clientConnectionHandler){}

void ReadFromSocket::run() const {
    bool result = true;
    while (result){
        result = clientConnectionHandler->getMessage();
    }
}

