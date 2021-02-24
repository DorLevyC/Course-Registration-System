
#include <iostream>
#include "../include/Message.h"


Message::Message(short _originalOpCode, short _receiveOpCode, std::string & _data): originalOpCode(_originalOpCode), receiveOpCode(_receiveOpCode), data(_data) {}

short Message::response() {
    if (receiveOpCode == 13){//ErrMessage
        std::cout << "ERROR " << originalOpCode << std::endl;
    }
    else {//AckMessage
        std::cout << "ACK " << originalOpCode << std::endl;
        if (data.length() > 0) {
            std::cout << data << std::endl;
        }
    }
    return originalOpCode;
}

short Message::getReceiveOpCode() {
    return receiveOpCode;
}

