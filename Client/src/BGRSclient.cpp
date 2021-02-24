#include <stdlib.h>
#include <iostream>
#include <thread>
#include "../include/ClientConnectionHandler.h"
#include "../include/ReadFromSocket.h"
#include "../include/ReadFromKeyboard.h"

int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ClientConnectionHandler clientConnectionHandler(host, port);

    if (!clientConnectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::cout << "Welcome to Course registrations system." << std::endl;
    std::cout << "To see the supported commands, enter: SHOWCOMMANDS" << std::endl;
    std::cout << "For full command description, go to: https://github.com/WhoIsDorLevy/Course-Registration-System" << std::endl;
    std::cout << "Please enter a command:" << std::endl;

    ReadFromSocket readFromSocket(clientConnectionHandler);
    ReadFromKeyboard readFromKeyboard(clientConnectionHandler);


    std::thread thread(&ReadFromSocket::run, &readFromSocket);
    readFromKeyboard.run();

    thread.join();

    std::cout << "Goodbye :)" << std::endl;

    return 0;
}
