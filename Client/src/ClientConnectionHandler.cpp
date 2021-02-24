
#include "../include/ClientConnectionHandler.h"

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ClientConnectionHandler::ClientConnectionHandler(string host, short port): host_(host), port_(port), io_service_(),
socket_(io_service_), messageDecoder(), clientMessageProtocol(), shouldTerminate(false){}

ClientConnectionHandler::~ClientConnectionHandler() {
    close();
}

bool ClientConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ClientConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ClientConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

// Close down the connection properly.
void ClientConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

bool ClientConnectionHandler::sendMessage(string &msg) {
    char* toSend;
    //space seperates command from additional data
    int index = msg.find_first_of(' ');
    string command = msg.substr(0, index);
    short opCode = - 1;
    short courseNum = - 1;

    //store the matching opCode, according to the keyboard input and it's data(if exists).
    if (command == "ADMINREG"){
        opCode = 1;
    }

    if (command =="STUDENTREG"){
        opCode = 2;
    }

    if (command == "LOGIN"){
        opCode = 3;
    }

    if (command == "LOGOUT"){
        opCode = 4;
    }

    if (command =="COURSEREG"){
        opCode = 5;
    }

    if (command =="KDAMCHECK"){
        opCode = 6;
    }

    if (command =="COURSESTAT"){
        opCode = 7;
    }

    if (command =="STUDENTSTAT"){
        opCode = 8;
    }

    if (command =="ISREGISTERED"){
        opCode = 9;
    }

    if (command =="UNREGISTER"){
        opCode = 10;
    }

    if (command == "MYCOURSES"){
        opCode = 11;
    }
    if (command == "SHOWCOMMANDS"){
        opCode = 0;
    }


    string data = msg.substr(index + 1);//additional data, might be empty for some commands

    if ((opCode == -1) & (!shouldTerminate)){//invalid command
        std::cout << "INVALID COMMAND" << std::endl;
        return !shouldTerminate;
    }

    else if (opCode == 0){

        std::cout << "0: SHOWCOMMANDS" << std::endl;
        std::cout << "1: ADMINREG <username> <password>" << std::endl;
        std::cout << "2: STUDENTREG <username> <password>" << std::endl;
        std::cout << "3: LOGIN <username> <password>" << std::endl;
        std::cout << "4: LOGOUT" << std::endl;
        std::cout << "5: COURSEREG <coursenum>" << std::endl;
        std::cout << "6: KDAMCHECK <coursenum>" << std::endl;
        std::cout << "7: COURSESTAT <coursenum>" << std::endl;
        std::cout << "8: STUDENTSTAT <username>" << std::endl;
        std::cout << "9: ISREGISTERED <coursenum>" << std::endl;
        std::cout << "10: UNREGISTER <coursenum>" << std::endl;
        std::cout << "11: MYCOURSES" << std::endl;
        std::cout << "Please enter a command:" << std::endl;

        return !shouldTerminate;
    }

    else if (((opCode < 4) | (opCode ==8)) | (opCode > 10)){//message contains string
        //spaces will be replaced later with matching opCode and '\0'
        data = "  " + data + " ";
        toSend = new char[data.size()];

        strcpy(toSend, data.c_str());//copy data to char array

        int space = data.substr(2).find_first_of(' ') + 2;//finds first space index
        int endIndex = data.size() - 1;

        if (opCode < 4){
            if (space >= endIndex){//no password provided
                return invalidCommand();
            }
            else {//space between username and password
                toSend[space] = '\0';//replace space
            }
        }
        toSend[data.size() - 1] = '\0';//replace space
    }

    else{//message should include courseNum
        if (!isValidInt(data)){
            return invalidCommand();
        }
        courseNum = stoi(data);//convert string to short
        toSend = new char[4];//2 bytes for opCode and 2 for CourseNum
        shortToChars(courseNum, toSend, 2);
    }
    shortToChars(opCode, toSend, 0);

    int bytesToWrite = data.length();//the number of the bytes to write = string length if exists

    if ((opCode == 4) | (opCode == 11)){//message contains opCode only => 2 bytes
        bytesToWrite = 2;
    }
    if (courseNum != -1){
        bytesToWrite = 4;//message includes only opCode and CourseNum => 4 bytes
    }

    bool result = sendBytes(toSend, bytesToWrite);
    delete[] toSend;
    if (!result){
        return false;
    }
    return !shouldTerminate;
}

bool ClientConnectionHandler::getMessage() {
    Message * message = nullptr;
    while (message == nullptr){
        char ch;
        if (!getBytes(&ch, 1)){
            return false;
        }
        message = messageDecoder.decodeNextChar(ch);
    }
    //message received

    clientMessageProtocol.process(*message);
    delete message;//process finished

    shouldTerminate = clientMessageProtocol.shouldTerminate();
    return !shouldTerminate;
}

void ClientConnectionHandler::shortToChars(short num, char* arr, int begin) {
    arr[begin] = (num >> 8) & 0xFF;
    arr[begin + 1] = num & 0xFF;
}

bool ClientConnectionHandler::invalidCommand() {
    std::cout << "INVALID COMMAND" << std::endl;
    return !shouldTerminate;
}

bool ClientConnectionHandler::isValidInt(std::string & data) {
    int size = data.size();
    if (size == 0){//empty string
        return false;
    }
    for (int i = 0; i < size; i++) {
        if ((data.at(i) > '9') || data.at(i) < '0'){
            return false;
        }
    }
    return true;
}