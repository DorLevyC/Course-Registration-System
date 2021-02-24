package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.CourseSystemEncoderDecoder;
import bgu.spl.net.impl.CourseSystemProtocol;
import bgu.spl.net.impl.Message;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args){
        Server<Message> server = Server.reactor(
                Integer.parseInt(args[1]),
                Integer.parseInt(args[0]),
                () ->  new CourseSystemProtocol(),
        () ->  new CourseSystemEncoderDecoder());
        server.serve();
    }
}
