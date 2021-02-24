package bgu.spl.net.impl;

public class AckMessage implements Message {
    private final short opCode;
    private final String data;

    public AckMessage(String data, short opCode){
        this.data = data;
        this.opCode = opCode;
    }


    @Override
    public Message response() {
        return null;
    }

    public short getOpCode() {
        return opCode;
    }
    public String getData(){
        return data;
    }
}
