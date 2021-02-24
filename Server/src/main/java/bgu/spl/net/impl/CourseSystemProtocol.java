package bgu.spl.net.impl;

import bgu.spl.net.api.MessagingProtocol;

public class CourseSystemProtocol implements MessagingProtocol<Message> {
    boolean shouldTerminate = false;
    @Override
    public Message process(Message msg) {
        Message response = msg.response();
        if (response instanceof AckMessage && ((AckMessage) response).getOpCode() == 4){
            //successful LOGOUT message
            shouldTerminate = true;
        }
        return response;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
