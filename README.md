# Course-Registration-System
A nice little project, simulating a university's course registration system, supporting both student and admin commands.

# Introduction
The system is based on client to server communication through the network. Once the server is initialized, an unlimited number of clients can connect to it, and each one can operate an unlimited of supported commands with the server.
Each valid command that is sent from the client to the server, will receive either an ACK(acknowledge) or ERR(error) message, followed by the original command's opcode, according to the protocol which will be described later.

# Requiements
For Linux users:
Server - Apache Maven 3.6.0 or higher.
Client - gcc 7.5.0 or higher.
*Notice that this code was not tested on Windows, but feel free to run it on any IDE of your liking.

# Launching the System
Clone the files to a destinated folder of your wish. We will refer to this location as "{installation home}".

(The following steps are not mandatory if you choose to run the code on IDE)
To launch the server, open the terminal, and run the following commands:
1. cd {installation home}/server
2. mvn clean
3. mvn compile
4. mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGRSServer.ReactorMain" -Dexec.args="<port> 5"
   (where port is a 4 digit number, which represents the port on your computer that the server will run on)

To launch the client, open the terminal, and run the following commands:
1. cd {installation home}/client
2. make clean
3. make
4. bin/BGRSclient <ip> <port>
   (where <ip> and <port> are the matching ip and port that your server runs on)

# Command description

	List of the supported commands:
	0: SHOWCOMMANDS
	1: ADMINREG <username> <password>
	2: STUDENTREG <username> <password>
	3: LOGIN <username> <password>
	4: LOGOUT
	5: COURSEREG <coursenum>
	6: KDAMCHECK <coursenum>
	7: COURSESTAT <coursenum>
	8: STUDENTSTAT <username>
	9: ISREGISTERED <coursenum>
	10: UNREGISTER <coursenum>
	11: MYCOURSES

	Users description:
	1: Anonymous user - a client which hasn't logged in yet.
	2: Admin user - a client which has logged in with an admin's username and password.
	3: Student user - a client which has logged in with a student's username and password.

Each command(according to it's description) can be executed by either of the following clients: Anonymous user, Admin user, Student user. If a client tries to execute a command that is not under his type(for example, an admin user tries to execute a student user-only command), an ERR message will be returned.

Commands description:

0- SHOWCOMMANDS

This command can be executed by any user.
This command does not interact with the server, and provides the list of the supported commands.


1- ADMINREG

An anonymous user command.
An ADMINREG message is used to register an admin in the service. If the username is already registered in the server, an
ERR message is returned. If successful an ACK message will be sent in return.

Parameters:

Opcode: 1

Username: The username to register in the server.

Password: The password for the current username (used to log in to the server).


Command initiation:

This command is initiated by entering the following text in the client command line interface: ADMINREG  <Username> <Password>


2- STUDENTREG

An anonymous user command.
A STUDENTREG message is used to register a student in the service. If the username is already registered in the server, an ERROR message is
returned. If successful an ACK message will be sent in return.

Parameters:

Opcode: 2

Username: The username to register in the server.

Password: The password for the current username (used to log in to the server).


Command initiation:

This command is initiated by entering the following text in the client command line interface: STUDENTREG  <Username> <Password>


3- LOGIN

An anonymous user command.
A LOGIN message is used to login a user into the server. If the user doesn’t exist or the password doesn’t match the one entered for the
username, sends an ERROR message. An ERROR message should also appear if the current client has already successfully logged in.

Parameters:

Opcode: 3

Username: The username to log in the server.

Password: The password for the current username (used to log in to the server)	     


Command initiation:
This command is initiated by entering the following text in the client command line interface: LOGIN <Username> <Password>


4- LOGOUT

This command can be executed by both student and admin users. 
Informs the server on client disconnection. A client terminates only after receiving an ACK message in replay. If no user is logged in, sends
an ERROR message.

Parameters:

Opcode: 4


Command initiation:

This command is initiated by entering the following text in the client command line interface: LOGOUT
Once the ACK command is received in the client, it terminates itself.



5- COURSEREG

A student user command.
Inform the server about the course the student want to register to, if the registration done successfully, an ACK message will be sent back to the client, otherwise, (e.g. no such course is exist, no seats are available in this course, the student does not have all the Kdam courses, the student is not logged in) ERR message will be sent back. (Note: the admin can’t register to courses, in case the admin sends a COURSEREG message, and ERR message will be sent back to the client).

Parameters:

Opcode: 5

Course Number: the number of the course the student wants to register to.


Command initiation:
This command is initiated by entering the following text in the client command line interface: COURSEREG <CourseNum>


6- KDAMCHECK

A student user command.
this message checks what are the KDAM courses of the specified course.
If student registered to a course successfully, we consider him having this course as KDAM.

Parameters:

Opcode: 6

Course Number: the number of the course the user needs to know its KDAM courses. When the server gets the message it returns the list of the KDAM courses(if there are now KDAM courses it returns an empty string).


Command initiation:
This command is initiated by entering the following texts in the client command line interface: KDAMCHECK <CourseNumber>


7- COURSESTAT

An Admin user command.
The admin sends this message to the server to get the state of a specific course.

The client prints the state of the course as followed:

Course: (<courseNum>) <courseName>

Seats Available: <numOfSeatsAvailable> / <maxNumOfSeats>

Students Registered: <listOfStudents> //ordered alphabetically


Example:

Course: (42) How To Train Your Dragon

Seats Available: 22/25

Students Registered: [ahufferson, hhhaddock, thevast] //if there are no students registered yet, simply prints []


Parameters:

Opcode: 7

Course Number: the number of the course we want the state of.


Command initiation:

This command is initiated by entering the following texts in the client command line interface: COURSESTAT <courseNum>


8- STUDENTSTAT

An admin user command.
A STUDENTSTAT message is used to receive a status about a specific student.

The client prints the state of the course as followed:

Student: <studentUsername>

Courses: <listOfCoursesNumbersStudentRegisteredTo>

Example:

Student: hhhaddock

Courses: [42] // if the student hasn’t registered to any course yet, simply prints []

Parameters:

Opcode: 8


Command initiation:

This command is initiated by entering the following texts in the client command line interface: STUDENTSTAT <StudentUsername>


9- ISREGISTERED

A student user command.
An ISREGISTERED message is used to know if the student is registered to the specified course.
The server sends back “REGISTERED” if the student is already registered to the course,
otherwise, it sends back “NOT REGISTERED”.

Parameters:

Opcode: 9

Course Number: The number of the course the student wants to check.


Command initiation:

This command is initiated by entering the following texts in the client command line interface: ISREGISTERED <courseNum>


10- UNREGISTER

A student user command.
An UNREGISTER message is used to unregister to a specific course.
The server sends back an ACK message if the registration process successfully done, otherwise, it sends back an ERR message.

Parameters:

Opcode: 10

Course Number: The number of the course the student wants to unregister to.


Command initiation:
This command is initiated by entering the following texts in the client command line interface: UNREGISTER <courseNum>


11- MYCOURSES

A student user command.
A MYCOURSES message is used to know the courses the student has registered to.
The server sends back a list of the courses number(in the format:[<coursenum1>,<coursenum2>]) that the student has registered to (could be empty []).

Parameters:

Opcode: 11


Command initiation:

This command is initiated by entering the following texts in the client command line interface: MYCOURSES



