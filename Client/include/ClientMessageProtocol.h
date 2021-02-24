

#ifndef CLIENT_CLIENTMESSAGEPROTOCOL_H
#define CLIENT_CLIENTMESSAGEPROTOCOL_H
#include "Message.h"

class ClientMessageProtocol {
public:
    void process(Message& msg);
    bool shouldTerminate() const;

private:
    bool shouldTerminateClient = false;
};


#endif //CLIENT_CLIENTMESSAGEPROTOCOL_H
