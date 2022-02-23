import java.util.*;
import java.sql.*;

public class Capacity{

	/** 
	* This method checks for int inputs from the scanner and verifies
	* they exist in the database with the prepared statement. 
	* @param scan Scanner for system.in
	* @param pstmt Prepared statement for the database including ?
	* @param label A label to prompt the user
	* @return the valid input
	*/
	static int int_input(Scanner scan, PreparedStatement pstmt, String label) throws SQLException{
		int input = 0;
		try{
			System.out.println("Enter "+label+": ");
			input = scan.nextInt();
		}catch(InputMismatchException e){
			System.out.println("Int expected, try again.");
			return 0;
		}
		if(input==0)System.exit(0);
		pstmt.setInt(1,input);
		ResultSet rset = pstmt.executeQuery();
		if(rset.next()){
			if(rset.getString("count(*)").equals("0")){
				System.out.println(label+" "+input+" not found in database");
				return 0;
			}
		}
		return input;
	}

	/** 
	* This method checks for string inputs from the scanner and verifies
	* they exist in the database with the prepared statement. 
	* @param scan Scanner for system.in
	* @param pstmt Prepared statement for the database including ?
	* @param label A label to prompt the user
	* @return the valid input
	*/
	static String str_input(Scanner scan, PreparedStatement pstmt, String label) throws SQLException{
		String input = "";
		try{
			System.out.println("Enter "+label+": ");
			input = scan.nextLine();
		}catch(InputMismatchException e){
			System.out.println("String expected, try again.");
			return "";
		}
		if(input.equals("0"))System.exit(0);
		pstmt.setString(1,input);
		ResultSet rset = pstmt.executeQuery();
		if(rset.next()){
			if(rset.getString("count(*)").equals("0")){
				System.out.println(label+" "+input+" not found in database");
				return "";
			}
		}
		return input;
	}

	public static void main(String[] args){
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter Oracle user id: ");
		String user_id = scan.nextLine();
		System.out.println("Enter Oracle password for " + user_id + ": ");
		String passwd = scan.nextLine();
		System.out.println("U_ID: "+user_id+" PASS: "+passwd);
		try (Connection conn = DriverManager.getConnection(
			"jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user_id, passwd);)
		{
			int year = 0;
			String semester = "";
			int c_id = 0;
			int s_id = 0;
			//do while for handling the flow of inputs
			do{
				System.out.println("Input data on the section whose classroom capacity you wish to check");
				year = int_input(scan, conn.prepareStatement("select count(*) from takes where year=?"),"Year");
				scan.nextLine();
				semester = "";
				if(year != 0){
					semester = str_input(scan, conn.prepareStatement("select count(*) from takes where year="+year+" and upper(semester)=upper(?)"),"Semester (Spring,Summer,Fall,Winter)");
					if(!semester.equals("")){
						c_id = int_input(scan, conn.prepareStatement("select count(*) from takes where year="+year+" and upper(semester)=upper('"+semester+"') and course_id=?"),"Course ID");
						if(c_id != 0){
							s_id = int_input(scan, conn.prepareStatement("select count(*) from takes where year="+year+" and upper(semester)=upper('"+semester+"') and course_id="+c_id+" and sec_id=?"),"Section ID");
						}
					}
				}
			}while(s_id==0);
			//Querying after input process is over
			int enrollment = 0;
			int capacity = 0;			
			Statement stmt = conn.createStatement();
			ResultSet rset = stmt.executeQuery("select count(*) from takes where year="+year+" and upper(semester)=upper('"+semester+"') and course_id="+c_id+" and sec_id="+s_id+" ");
			if(rset.next()) {
				enrollment = rset.getInt("count(*)");
				if (rset.wasNull()) System.out.println("Got null value");
			}
			rset = stmt.executeQuery("select capacity from section,classroom where year="+year+" and upper(semester)=upper('"+semester+"') and course_id="+c_id+" and sec_id="+s_id+" and section.building=classroom.building and section.room_number=classroom.room_number");
			if (rset.next()) {
				capacity = rset.getInt("capacity");
				if (rset.wasNull()) System.out.println("Got null value");
			}
			rset = stmt.executeQuery("select capacity from section,classroom where course_id="+c_id+" and sec_id="+s_id+" and section.building=classroom.building and section.room_number=classroom.room_number");
			if (rset.next()) {
				capacity = rset.getInt("capacity");
				if (rset.wasNull()) System.out.println("Got null value");
			}
			System.out.println("Capacity is "+capacity+" and enrollment is "+enrollment);
			if(capacity>enrollment){
				System.out.println("There are "+(capacity-enrollment)+" seats open.");
			}
			else{
				System.out.println("There are not enough seats");
			}
		}
		catch (SQLException sqle) {
			System.out.println("SQLException : " + sqle);
		}
	}
}