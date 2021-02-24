package bgu.spl.net.impl;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.Database;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class CourseSystemEncoderDecoder implements MessageEncoderDecoder<Message> {

    private short opCode = - 1;
    private int countEnding = 1;//signals end of message
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;
    private String username = null;//will be updated after login
    private short courseNum = -1;

    @Override
    public Message decodeNextByte(byte nextByte) {

        if (opCode == -1) {//opCode hasn't been found yet
            pushByte(nextByte);
            if (len == 2){//decode opCode
                opCode = bytesToShort();
                if (opCode >= 1 & opCode <= 3){
                    countEnding++;//there are two strings in the message
                }
                if (opCode == 4 | opCode == 11){//no additional data besides opCode
                    Message output = popMessage();
                    opCode = -1;//reset for next message to come
                    return output;
                }
            }
            return null;
        }

        //now opCode is updated

        if (opCode >= 5 & opCode <=10 & opCode != 8){//message contains short courseNum
            if (len == 1){//last byte of short
                pushByte(nextByte);
                countEnding--;
            }
        }
        else if (nextByte == 0){//end of String
            countEnding--; 
        }

        if (countEnding == 0) {//end of message
            Message output = popMessage();
            //reset opCode and countEnding for the next message to arrive
            opCode = -1;
            countEnding = 1;
            return output;
        }

        pushByte(nextByte);
        return null;


    }

    @Override
    public byte[] encode(Message message) {
        byte[] output = null;
        if (message instanceof ErrMessage) {
            byte[] errOp = shortToBytes((short) 13);//encode Err opCode
            byte[] messageOp = shortToBytes(((ErrMessage) message).getOpCode());//encode original message opCode
            //merge two arrays to one output array
            output = new byte[errOp.length + messageOp.length];
            System.arraycopy(errOp, 0, output, 0, errOp.length);
            System.arraycopy(messageOp, 0, output, errOp.length, messageOp.length);
        }
        else {
            byte[] ackOp = shortToBytes((short) 12);//encode Ack opCode
            byte[] messageOp = shortToBytes(((AckMessage) message).getOpCode());//encode original message opCode
            byte[] data = ((AckMessage) message).getData().getBytes();//encode additional data, UTF8 by default
            //merge three arrays to one output array
            output = new byte[data.length + ackOp.length + messageOp.length];
            System.arraycopy(ackOp, 0, output, 0, ackOp.length);
            System.arraycopy(messageOp, 0, output, ackOp.length, messageOp.length);
            System.arraycopy(data, 0, output, ackOp.length + messageOp.length, data.length);
        }
        return output;
    }

    private void pushByte(byte nextByte){
        if (len >= bytes.length){
            bytes = Arrays.copyOf(bytes, bytes.length * 2);
        }
        bytes[len++] = nextByte;
    }

    private Message popMessage() {
        Database base = Database.getInstance();
        //save the last opCode(will be reset later)
        short lastOpCode = opCode;

        switch (lastOpCode) {
            case 1: {//ADMINREG message
                String arg = popString();//username & password
                return () -> {
                    if (username == null) {//before registration, client can't be logged in
                        String usernameReg = arg.substring(0, arg.indexOf('\0'));
                        String passwordReg = arg.substring(arg.indexOf('\0') + 1);
                        boolean success = base.registerAdmin(usernameReg, passwordReg);
                        if (success) {
                            return new AckMessage("\0", lastOpCode);
                        }
                    }
                    return new ErrMessage(lastOpCode);
                };
            }
            case 2: {//STUDENTREG message
                String arg = popString();//username & password
                return () -> {
                    if (username == null) {//before registration, client can't be logged in
                        String usernameReg = arg.substring(0, arg.indexOf('\0'));
                        String passwordReg = arg.substring(arg.indexOf('\0') + 1);
                        boolean success = base.registerStudent(usernameReg, passwordReg);
                        if (success) {
                            return new AckMessage("\0", lastOpCode);
                        }
                    }
                    return new ErrMessage(lastOpCode);
                };
            }

            case 3: {//LOGIN message
                String arg = popString();//username & password
                return () -> {
                    if (username == null) {//before login, client can't be logged in
                        String usernameLog = arg.substring(0, arg.indexOf('\0'));
                        String passwordLog = arg.substring(arg.indexOf('\0') + 1);
                        boolean success = base.login(usernameLog, passwordLog);
                        if (success) {
                            username = usernameLog;
                            return new AckMessage("\0", lastOpCode);
                        }
                    }
                    return new ErrMessage(lastOpCode);
                };
            }
            case 4: {//LOGOUT message
                len = 0;
                return () -> {
                    if (!base.logout(username)) {
                        return new ErrMessage(lastOpCode);
                    }
                    return new AckMessage("\0", lastOpCode);
                };
            }

            case 5: {//COURSEREG message
                int courseNum = bytesToShort();
                return () -> {
                    if (!base.registerToCourse(courseNum, username)) {
                        return new ErrMessage(lastOpCode);
                    }
                    return new AckMessage("\0", lastOpCode);
                };
            }

            case 6: {//KDAMCHECK message
                int courseNum = bytesToShort();
                return () -> {
                    String kdamCourses = "";
                    //already sorted by the order in the file
                    List<Integer> kdam = base.getKdamCoursesNums(courseNum, username);
                    if (kdam == null){
                        return new ErrMessage(lastOpCode);
                    }
                    for (Integer course : kdam) {
                        kdamCourses = kdamCourses + "," + course;
                    }
                    if (!kdamCourses.equals("")){
                        kdamCourses = kdamCourses.substring(1);
                    }
                    kdamCourses = "[" + kdamCourses + "]";
                    return new AckMessage(kdamCourses + "\0", lastOpCode);
                };
            }

            case 7: {//COURSESTAT message
                int courseNum = bytesToShort();
                return () -> {
                    //Course num + name
                    String courseStat = "Course: (" + courseNum + ") ";
                    String courseName = base.getCourseName(courseNum, username);

                    if (courseName == null){ //user is not admin or course doesn't exist
                        return new ErrMessage(lastOpCode);
                    }
                    //seats available:
                    courseStat = courseStat + courseName + '\n' + "Seats Available: ";
                    String seats = base.getAvailableSeats(courseNum, username) + "/" + base.getMaxNumOfStudents(courseNum, username);
                    courseStat = courseStat + seats + '\n';

                    //Students registered:
                    //already sorted alphabetically
                    List<String> registered = base.getRegisteredStudents(courseNum, username);
                    courseStat = courseStat + "[";
                    if (!registered.isEmpty()) {
                        for (String student : registered) {
                            courseStat = courseStat + student + ",";
                        }
                        courseStat = courseStat.substring(0, courseStat.length() - 1);
                    }
                    courseStat = courseStat + "]";

                    return new AckMessage(courseStat + "\0", lastOpCode);
                };
            }

            case 8: {//STUDENTSTAT message
                String name = popString();//username
                return () -> {
                    String studentStat = "Student: " + name + '\n';
                    String courses = "";

                    //already sorted in the same order as in the courses file
                    List<Integer> courseList = base.getCoursesRegistered(name, username);

                    if (courseList == null || !base.isAdmin(username)){//Only admin can request this message
                        return new ErrMessage(lastOpCode);
                    }
                    for (Integer course : courseList){
                        courses = courses + "," + course;
                    }
                    if (!courses.equals("")){
                        courses = courses.substring(1);
                    }
                    courses = "[" + courses + "]";
                    studentStat = studentStat + "Courses: " + courses;
                    return new AckMessage(studentStat + '\0', lastOpCode);
                };
            }

            case 9: {//ISREGESTERED message
                int courseNum = bytesToShort();
                return () -> {
                    if (!base.isLoggedIn(username) || base.isAdmin(username)) {
                        return new ErrMessage(lastOpCode);
                    }
                    if (base.isRegisteredToCourse(courseNum, username)) {
                        return new AckMessage("REGISTERED" + "\0", lastOpCode);
                    }
                    //in any other case, return "NOT REGISTERED"
                    return new AckMessage("NOT REGISTERED" + "\0", lastOpCode);
                };
            }

            case 10: {//UNREGISTER message
                int courseNum = bytesToShort();
                return () -> {
                    if (!base.unRegisterToCourse(courseNum, username)) {
                        return new ErrMessage(lastOpCode);
                    }
                    return new AckMessage("\0", lastOpCode);
                };
            }

            case 11: { //MYCOURSES message
                len = 0;
                return () -> {
                    String myCourses = "";

                    //already sorted in the same order as in the courses file
                    List<Integer> courses = base.getCoursesRegistered(username, username);

                    if (courses == null){
                        return new ErrMessage(lastOpCode);
                    }
                    for (Integer course : courses) {
                        myCourses = myCourses + "," + course;
                    }
                    if (!courses.isEmpty()){
                        myCourses = myCourses.substring(1);
                    }
                    return new AckMessage("[" + myCourses + "]" + "\0", lastOpCode);
                };
            }
            default: {
                return null;
            }

        }
    }


    private short bytesToShort(){
        short result = (short)((bytes[0] & 0xff) << 8);
        result += (short)(bytes[1] & 0xff);
        len = 0;
        return result;
    }

    private byte[] shortToBytes(short num){
        byte[] output = new byte[2];
        output[0] = (byte)((num >> 8) & 0xFF);
        output[1] = (byte)(num & 0xFF);
        return output;
    }

    private String popString(){
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        //reset fields
        len = 0;
        countEnding = 1;
        return result;
    }

}
