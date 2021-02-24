
#ifndef CLIENT_MESSAGEDECODER_H
#define CLIENT_MESSAGEDECODER_H

#include "Message.h"
#include <vector>
class MessageDecoder {

public:
    MessageDecoder();
    Message* decodeNextChar(char nextChar);

private:
    void pushChar(char nextChar);
    Message* popMessage();
    short charsToShort();
    void resetFields();
    int len;
    short receivedOpCode;
    short originalOpCode;
    std::vector<char> chars;
    std::string data;


};



#endif //CLIENT_MESSAGEDECODER_H
