#include <codecvt>
#include "../include/MessageDecoder.h"
using namespace std;
MessageDecoder::MessageDecoder(): len(0), receivedOpCode(-1), originalOpCode(-1), chars(), data(){}

Message* MessageDecoder::decodeNextChar(char nextChar) {
    pushChar(nextChar);
    if ((len == 2) & (receivedOpCode == -1)){//decode received opCode
        receivedOpCode = charsToShort();
        len = 0;
        chars.clear();
    }

    if ((originalOpCode == -1) & (len == 2)){//decode original opCode
        originalOpCode = charsToShort();
        len = 0;
        chars.clear();
    }

    if ((receivedOpCode == 13) & (originalOpCode != -1)){//return new Err Message
        string receive = "";//Err message => no data
        Message* output = new Message(originalOpCode, receivedOpCode, receive);
        resetFields();
        return output;
    }

    if ((nextChar == '\0') & (originalOpCode != -1)){//return Ack message
        Message* output = popMessage();
        resetFields();
        return output;
    }
    return nullptr;
}

void MessageDecoder::pushChar(char nextChar) {
    chars.push_back(nextChar);
    len++;
}

Message *MessageDecoder::popMessage() {
    //do not add null char to data(always last)
    for (int i = 0; i < len -1; i++) {
        data.append(1, chars.at(i));
    }
    return new Message(originalOpCode, receivedOpCode, data);
}

short MessageDecoder::charsToShort() {
    short result = (short)((chars.at(0) & 0xff) << 8);
    result += (short)(chars.at(1) & 0xff);
    return result;
}

void MessageDecoder::resetFields() {
    len = 0;
    receivedOpCode = -1;
    originalOpCode = -1;
    data = "";
    chars.clear();
}


