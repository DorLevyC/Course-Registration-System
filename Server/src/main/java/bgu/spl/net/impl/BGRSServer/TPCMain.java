package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.CourseSystemEncoderDecoder;
import bgu.spl.net.impl.CourseSystemProtocol;
import bgu.spl.net.impl.Message;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args){
        Server<Message> server = Server.threadPerClient(Integer.parseInt(args[0]),
                () ->  new CourseSystemProtocol(),
                () ->  new CourseSystemEncoderDecoder());
        server.serve();
    }
}
