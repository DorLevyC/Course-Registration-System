package bgu.spl.net.srv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {

	//courses fields
	private final HashMap<Integer, String> courseNames;
	private final HashMap<Integer, List<Integer>> kdamCourses;//list of kdam courses for each course
	private final HashMap<Integer, Integer> maxStudents;//num of maximum students for this course
	private final HashMap<Integer, List<String>> studentsRegistered;//num of currently registered students to this course
	private final HashMap<Integer, Integer> linePos;//line number in the original courses.txt file

	//students fields
	private final HashMap<String, String> studentPasswords;//password of the student
	private final HashMap<String, List<Integer>> studentRegistrations;//which courses is the student registered

	//admin fields
	private final HashMap<String, String> adminPasswords;

	//admin & student fields
	private final HashMap<String, Boolean> loggedIn;//is the user logged in or not

	private static Database singleton;
	//to prevent user from creating new Database
	private Database() {

		//initialize fields
		courseNames = new HashMap<>();
		kdamCourses = new HashMap<>();
		maxStudents = new HashMap<>();
		studentsRegistered = new HashMap<>();
		studentPasswords = new HashMap<>();
		studentRegistrations = new HashMap<>();
		loggedIn = new HashMap<>();
		adminPasswords = new HashMap<>();
		linePos = new HashMap<>();

		//read from courses file
		initialize("./Courses.txt");
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static synchronized Database getInstance() {
		if (singleton == null){
			singleton = new Database();
		}
		return singleton;
	}
	
	/**
	 * loads the courses from the file path specified
	 * into the Database, returns true if successful.
	 */
	boolean initialize(String coursesFilePath) {
		try {
			File courses = new File(coursesFilePath);

			Scanner reader1 = new Scanner(courses);
			Scanner reader2 = new Scanner(courses);
			int lineNum = 0;

			//update the linepos for every course:
			while (reader1.hasNextLine()){
				String course = reader1.nextLine();
				int courseNum = Integer.parseInt(course.substring(0, course.indexOf('|')));
				linePos.put(lineNum, courseNum);
				lineNum++;
			}

			//update courses:
			while (reader2.hasNextLine()){
				String course = reader2.nextLine();


				//get the number of the course from the line
				int prevIndex = course.indexOf('|');
				int courseNum = Integer.parseInt(course.substring(0,prevIndex));

				//get the name of the course from the line
				int nextIndex = course.indexOf('|', prevIndex + 1);
				courseNames.put(courseNum, course.substring(prevIndex + 1,nextIndex));


				//get the kdam courses list from the line
				prevIndex = nextIndex;
				nextIndex = course.indexOf('|', prevIndex + 1);
				String kdamAsString = course.substring(prevIndex + 2, nextIndex - 1);
				String[] kdamCoursesStrings = kdamAsString.split(",");
				LinkedList<Integer> kdamCoursesInt = new LinkedList<>();

				if (!kdamCoursesStrings[0].equals("")) {
					//add the kdam courses by the order they were listed in the courses file
					for (int j = 0; j < lineNum; j++) {
						for (int i = 0; i < kdamCoursesStrings.length; i++) {
							if (Integer.parseInt(kdamCoursesStrings[i]) == linePos.get(j)) {
								kdamCoursesInt.addLast(linePos.get(j));
							}
						}
					}
				}
				kdamCourses.put(courseNum, kdamCoursesInt);

				//get the max students number from the line
				maxStudents.put(courseNum, Integer.parseInt(course.substring(nextIndex + 1)));

				//initialize new students list for each course
				studentsRegistered.put(courseNum, new LinkedList<>());
			}
		} catch (FileNotFoundException ex){
			return false;
		}

		return true;
	}

	public List<Integer> getKdamCoursesNums(int courseNum, String username) {
		if ( !isLoggedIn(username) ||
				!studentPasswords.containsKey(username) //username is not a student
				|| !courseNames.containsKey(courseNum)) {//course doesn't exist
			return null;
		}
		return kdamCourses.get(courseNum);
	}

	public List<String> getRegisteredStudents(int courseNum, String username) {
		if (!isLoggedIn(username) || !isAdmin(username)//user is not an admin or not logged in
				|| !courseNames.containsKey(courseNum)) {//course doesn't exist
			return null;
		}

		return studentsRegistered.get(courseNum);
	}

	public int getMaxNumOfStudents(int courseNum, String username){
		if (!isLoggedIn(username) || !isAdmin(username)//user is not an admin or not logged in
				|| !courseNames.containsKey(courseNum)) {//course doesn't exist
			return -1;
		}
		return maxStudents.get(courseNum);
	}

	public int getAvailableSeats(int courseNum, String username){
		if (!isLoggedIn(username) || !isAdmin(username)//user is not an admin or not logged in
				|| !courseNames.containsKey(courseNum)) {//course doesn't exist
			return -1;
		}
		return maxStudents.get(courseNum) - studentsRegistered.get(courseNum).size();
	}

	public String getCourseName(int courseNum, String username){
		if (!isLoggedIn(username) || !isAdmin(username)//user is not an admin or not logged in
				|| !courseNames.containsKey(courseNum)) {//course doesn't exist
			return null;
		}
		return courseNames.get(courseNum);
	}

	public List<Integer> getCoursesRegistered(String student, String username){
		//only admin and myself can check my courses
		if (!isLoggedIn(username)
				|| (!username.equals(student)
					&& !isAdmin(username))){//only me and admin can access my courses
			return null;
		}
		return studentRegistrations.get(student);
	}

	public boolean isLoggedIn(String username){
		if (username == null || loggedIn.get(username) == null){
			return false;
		}
		return loggedIn.get(username);
	}

	public boolean login(String username, String password){
		synchronized (loggedIn) {
			String studentCorrectPassword = studentPasswords.get(username);
			String adminCorrectPassword = adminPasswords.get(username);

			if ((!password.equals(studentCorrectPassword) & !password.equals(adminCorrectPassword)) || isLoggedIn(username)) {
				return false;
			}
			loggedIn.replace(username, true);
			return true;
		}
	}

	public synchronized boolean registerStudent(String username, String password){
		if(studentPasswords.get(username) != null || adminPasswords.get(username) != null){//username already exists
			return false;
		}
		studentPasswords.put(username, password);
		studentRegistrations.put(username, new LinkedList<>());
		loggedIn.put(username, false);
		return true;
	}

	public boolean registerToCourse(int courseNum, String username){
		if (!isLoggedIn(username) //student is not logged in
				|| !courseNames.containsKey(courseNum) //course doesn't exist
				|| isRegisteredToCourse(courseNum, username) // student already registered to this course
				|| maxStudents.get(courseNum) <= studentsRegistered.get(courseNum).size() // course is full
				|| isAdmin(username) //username is an admin
		) {
			return false;
		}

		for (Integer kdam : kdamCourses.get(courseNum)){
			if (!isRegisteredToCourse(kdam, username)){ //student doesn't have all the needed kdam courses
				return false;
			}
		}

		//all conditions are satisfied, register to course
		synchronized(studentsRegistered) {
			studentsRegistered.get(courseNum).add(username);
			Collections.sort(studentsRegistered.get(courseNum));
		}

		synchronized (studentRegistrations) {
			studentRegistrations.get(username).add(courseNum);
		}

		return true;
	}

	public boolean isRegisteredToCourse(int courseNum, String username){
		if (!courseNames.containsKey(courseNum)){
			return false;//course doesn't exist
		}
		return studentRegistrations.containsKey(username) && studentRegistrations.get(username).contains(courseNum);
	}

	public boolean logout(String username) {
		synchronized (loggedIn) {
			if (!isLoggedIn(username)) {
				return false;
			}
			loggedIn.replace(username, false);
			return true;
		}
	}

	public boolean unRegisterToCourse(Integer courseNum, String username){
		if (!isLoggedIn(username) || !isRegisteredToCourse(courseNum, username)){
			return false;
		}
		synchronized (studentRegistrations) {
			studentRegistrations.get(username).remove(courseNum);
		}
		synchronized (studentsRegistered){
			studentsRegistered.get(courseNum).remove(username);
		}
		return true;
	}

	public boolean registerAdmin(String username, String password){
		if(studentPasswords.get(username) != null || adminPasswords.get(username) != null){//username already exists
			return false;
		}
		synchronized (adminPasswords) {
			adminPasswords.put(username, password);
		}
		synchronized (loggedIn) {
			loggedIn.put(username, false);
		}
		return true;
	}

	public boolean isAdmin(String username){
		return adminPasswords.containsKey(username);
	}

}
