
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class ResetDatabase
 */
@WebServlet("/ResetDatabase")
public class ResetDatabase extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResetDatabase() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void insertWords() throws Exception{
  		
		String s,sql;
		int c=0;
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			PreparedStatement psmt;
			
 	    	try {
 	    		URL url = new URL("http://deepakbaghel.in/TMS/english_words.txt");
 	    		URLConnection urlconnection = url.openConnection();
 	    		BufferedReader br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
 	    		
 	    		smt.executeUpdate("drop table if exists words");
 	    		sql="create table words "+
 						"(id int(10) unsigned not null auto_increment,"+
 						"author varchar(45) not null,"+
 						"word varchar(45) not null,"+
 						"used int(10) unsigned not null,"+
 						"datetime datetime not null,"+
 						"primary key(id))";
 	 			smt.executeUpdate(sql);
 	 			psmt=cn.prepareStatement("insert into words values (null,'admin123',?,0,now())");
 	 			
 	 			while((s=br.readLine()) != null){
 	 				psmt.setString(1, s);
 	 				psmt.executeUpdate();
 	 				c++;
 	 			}
 	 			psmt.close();
 	    	} catch(Exception e){}
 			
 			smt.close();cn.close();
 			System.out.println("added "+c+" words");
		} catch(Exception e){}
    }
    
    protected void insertCountries() throws Exception{
    	
 		String s,sql;
 	
 		int c=0;
 		try{
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
 			Statement smt=cn.createStatement();
 			PreparedStatement psmt;
 			
 	    	try {
 	    		URL url = new URL("http://deepakbaghel.in/TMS/countries.txt");
 	    		URLConnection urlconnection = url.openConnection();
 	    		BufferedReader br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
 	    		
 	    		smt.executeUpdate("drop table if exists countries");
 	 			sql="create table countries "+
 	 					"(id int(10) unsigned not null auto_increment,"+
 	 					"name varchar(45) not null,"+
 	 					"primary key(id))";
 	 			smt.executeUpdate(sql);
 	 			psmt=cn.prepareStatement("insert into countries values (null,?)");
 	 			
 	 			while((s=br.readLine()) != null){
 	 				psmt.setString(1, s);
 	 				psmt.executeUpdate();
 	 				c++;
 	 			}
 	 			psmt.close();
 	    	} catch(Exception e){}
 			
 			smt.close();cn.close();
 		}catch(Exception e){}
 	//	br.close();
 		System.out.println("added "+c+" countries");
     }
    
    
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pr=response.getWriter();
		String s;
		s="<html>"+
				"<body>"+
					"<form action=\"ResetDatabase\" method=\"post\">"+
						"<table>"+
							"<tr>"+
								"<td>Mysql user name:</td><td> <input type=text value=\"root\" name=userid></td>"+
							"</tr>"+
							"<tr>"+
								"<td>Mysql user password:</td><td> <input type=password name=password></td>"+
							"</tr>"+
							"<tr>"+
								"<td>admin password:</td><td> <input type=password name=adminpassword></td>"+
							"</tr>"+
							"<tr>"+
								"<td></td><td><input type=submit value=\"Create database\"></td>"+
							"</tr>"+
						"</table>"+
					"</form>"+
				"<body>"+
			"</html>";
		pr.println(s);
		pr.flush();
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String Q,user,password,adminpassword;
		PrintWriter pr=response.getWriter();
		user=request.getParameter("userid");
		password=request.getParameter("password");
		adminpassword=request.getParameter("adminpassword");
		if(adminpassword.equals("D6.hy98HJf53Y5jGt")==false) return;
		
		try{
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS",user,password);
			Statement smt=cn.createStatement();
			smt.executeUpdate("drop database if exists deepakba_tms");
			smt.executeUpdate("create database deepakba_tms");
			smt.executeUpdate("use deepakba_tms");
			
			Q="create table active_admin "+
					"(id int(10) unsigned not null auto_increment,"+
					"`admin_id` varchar(45) not null,"+
					"`session_key` varchar(50) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table active_users "+
					"(id int(10) unsigned not null auto_increment,"+
					"`user_id` varchar(45) not null,"+
					"user_fname varchar(45) not null,"+
					"user_lname varchar(45) not null,"+
					"`session_key` varchar(50) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table admin "+
					"(id int(10) unsigned not null auto_increment,"+
					"admin_id varchar(45) not null,"+
					"admin_pass varchar(45) not null,"+
					"admin_fname varchar(45) not null,"+
					"admin_lname varchar(45) not null,"+
					"admin_country varchar(45) not null,"+
					"admin_gender varchar(45) not null,"+
					"admin_about varchar(500) not null,"+
					"admin_email varchar(45) not null,"+
					"facebook_link varchar(100) not null,"+
					"google_link varchar(100) not null,"+
					"github_link varchar(100) not null,"+
					"quora_link varchar(100) not null,"+
					"last_login datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table admin_messages ("+
					"id int(10) unsigned not null auto_increment,"+
					"type varchar(45) not null,"+
					"sender varchar(45) not null,"+
					"message varchar(5000) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table answers "+
					"(id int(10) unsigned not null auto_increment,"+
					"question_id int(10) unsigned not null,"+
					"author varchar(45) not null,"+
					"answer varchar(5000) not null,"+
					"upvote int(10) unsigned not null,"+
					"downvote int(10) unsigned not null,"+
					"datetime datetime not null,"+
					"commentcount int(10) unsigned not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table answer_downvote "+
					"(id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"primary key(id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table answer_upvote "+
					"(id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"primary key(id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table comments "+
					"(id int(10) unsigned not null auto_increment,"+
					"author varchar(45) not null,"+
					"type varchar(20) not null,"+
					"typeid int(10) unsigned not null,"+
					"comment varchar(200) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table countries "+
					"(id int(10) unsigned not null auto_increment,"+
					"name varchar(45) not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table inbox "+
					"(id int(10) unsigned not null auto_increment,"+
					"sender varchar(45) not null,"+
					"receiver varchar(45) not null,"+
					"message varchar(200) not null,"+
					"status tinyint(1) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table notes "+
					"(id int(10) unsigned not null auto_increment,"+
					"user_id varchar(45) not null,"+
					"notes varchar(2000) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table notifications "+
					"(id int(10) unsigned not null auto_increment,"+
					"author varchar(45) not null,"+
					"status tinyint(1) not null,"+
					"datetime datetime not null,"+
					"notification varchar(400) not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table outbox "+
					"(id int(10) unsigned not null auto_increment,"+
					"sender varchar(45) not null,"+
					"receiver varchar(45) not null,"+
					"message varchar(200) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table questions "+
					"(id int(10) unsigned not null auto_increment,"+
					"author varchar(45) not null,"+
					"upvote int(10) unsigned not null,"+
					"downvote int(10) unsigned not null,"+
					"question varchar(200) not null,"+
					"datetime datetime not null,"+
					"answercount int(10) unsigned not null,"+
					"commentcount int(10) unsigned not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table question_downvote "+
					"(id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"primary key(id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table question_upvote "+
					"(id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"primary key(id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table readinglist "+
					"(question_id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"question varchar(200) not null,"+
					"datetime datetime not null,"+
					"primary key(question_id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table recycle_bin "+
					"(id int(10) unsigned not null auto_increment,"+
					"type varchar(20) not null,"+
					"typeid int(10) unsigned not null,"+
					"author varchar(45) not null,"+
					"datetime datetime not null,"+
					"text varchar(5100) not null,"+
					"deleted_on datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table remove_reminder "+
					"(task_id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"user_email varchar(45) not null,"+
					"verify_key varchar(50) not null,"+
					"datetime datetime not null,"+
					"primary key(task_id, user_id))";
			smt.executeUpdate(Q);
			
			Q="create table reset_password "+
					"(id int(10) unsigned not null auto_increment,"+
					"`user_id` varchar(45) not null,"+
					"`reset_key` varchar(50) not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table reset_session "+
					"(id int(10) unsigned not null auto_increment,"+
					"`user_id` varchar(45) not null,"+
					"`reset_key` varchar(50) not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table suspicious "+
					"(id int(10) unsigned not null,"+
					"type varchar(45) not null,"+
					"reporter varchar(45) not null,"+
					"primary key(id,type,reporter))";
			smt.executeUpdate(Q);
			
			Q="create table tags "+
					"(id int(10) unsigned not null auto_increment,"+
					"author varchar(45) not null,"+
					"tag varchar(45) not null,"+
					"used int(10) unsigned not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table tag_map "+
					"(tag_id int(10) unsigned not null,"+
					"question_id int(10) unsigned not null,"+
					"datetime datetime not null,"+
					"primary key(tag_id,question_id))";
			smt.executeUpdate(Q);
			
			Q="create table tag_user_map "+
					"(tag_id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"datetime datetime not null,"+
					"primary key(tag_id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table tag_task_map "+
					"(tag_id int(10) unsigned not null,"+
					"task_id int(10) unsigned not null,"+
					"datetime datetime not null,"+
					"primary key(tag_id,task_id))";
			smt.executeUpdate(Q);
			
			Q="create table task_user_map "+
					"(task_id int(10) unsigned not null,"+
					"user_id varchar(45) not null,"+
					"mail_count int(10) unsigned not null,"+
					"datetime datetime not null,"+
					"primary key(task_id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table task "+
					"(id int(10) unsigned not null auto_increment,"+
					"author varchar(45) not null,"+
					"type varchar(45) not null,"+
					"heading varchar(100) not null,"+
					"description varchar(500) not null,"+
					"privacy varchar(45) not null,"+
					"date date not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table users "+
					"(id int(10) unsigned not null auto_increment,"+
					"user_id varchar(45) not null,"+
					"user_pass varchar(45) not null,"+
					"user_fname varchar(45) not null,"+
					"user_lname varchar(45) not null,"+
					"user_country varchar(45) not null,"+
					"user_gender varchar(45) not null,"+
					"user_about varchar(200) not null,"+
					"user_email varchar(45) not null,"+
					"facebook_link varchar(100) not null,"+
					"google_link varchar(100) not null,"+
					"github_link varchar(100) not null,"+
					"quora_link varchar(100) not null,"+
					"last_login datetime not null,"+
					"primary key(id,user_id))";
			smt.executeUpdate(Q);
			
			Q="create table verify_email "+
					"(id int(10) unsigned not null auto_increment,"+
					"`user_id` varchar(45) not null,"+
					"`verify_key` varchar(50) not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table words "+
					"(id int(10) unsigned not null auto_increment,"+
					"author varchar(45) not null,"+
					"word varchar(45) not null,"+
					"used int(10) unsigned not null,"+
					"datetime datetime not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			Q="create table word_predict ("+
					"id int(10) unsigned not null auto_increment,"+
					"first varchar(45) not null,"+
					"second varchar(45) not null,"+
					"used int(10) unsigned not null,"+
					"primary key(id))";
			smt.executeUpdate(Q);
			
			//smt.executeUpdate("insert into countries values (1, 'India')");
			Q="insert into admin (admin_id,admin_pass,admin_fname,admin_lname,admin_country,admin_gender,admin_about,admin_email,facebook_link, google_link, github_link,quora_link, last_login) values ('admin123','d','Deepak','Baghel','India','Male','coder','deepakbaghel31@gmail.com','https://www.facebook.com/adm1n123','https://plus.google.com/109397123452819891808','https://github.com/adm1n123','https://www.quora.com/profile/Deepak-Baghel-6',now())";
			smt.executeUpdate(Q);		
			pr.println("<html><font size=18 color=green>Database TMS created</br></font><font size=13 color=orange>admin account also created :)</font></br>1.<b><i>Id:</i></b> admin123</br>&nbsp;<b><i>Password:</i></b> d</br></html>");
			
			insertCountries();
			insertWords();
		}catch(Exception e){
			pr.println(e);
		}
		
	}
	
}
