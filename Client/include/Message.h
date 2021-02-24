
#ifndef CLIENT_MESSAGE_H
#define CLIENT_MESSAGE_H

#include <string>

class Message{

public:
    Message(short _originalOpCode, short _receiveOpCode, std::string & _data);
    short response();
    short getReceiveOpCode();

private:
    short originalOpCode;
    short receiveOpCode;
    std::string data;


};

#endif //CLIENT_MESSAGE_H
