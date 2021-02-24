package bgu.spl.net.impl;

public class ErrMessage implements Message {

    private final short opCode;


    public ErrMessage(short opCode) {
        this.opCode = opCode;
    }

    @Override
    public Message response() {
        return null;
    }

    public short getOpCode() {
        return opCode;
    }
}
