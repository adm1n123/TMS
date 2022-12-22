

import java.io.IOException;
import java.io.PrintWriter;
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
 * Servlet implementation class AddTask
 */
@WebServlet("/AddTask")
public class AddTask extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddTask() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions(); fob.noCache(response);
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String remainder="",t,Head,Navbar,who,page="userhome",userid,s, heading, description, tags[], tag, type, privacy,date, taskid;
		String id="",Q;
		int count=0, tl=0,tagid;
		boolean valid=true;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		heading = request.getParameter("heading").trim();
		description = request.getParameter("description").trim();
		tag = request.getParameter("tag");
		type = request.getParameter("type");
		privacy = request.getParameter("privacy");
		date = fob.parseDate(request.getParameter("date"));
		
		if(heading.length() > 200) heading = heading.substring(0, 200);
		if(description.length() > 500) description = description.substring(0, 500);
		
		if(heading.length() == 0 || date.length() == 0) valid = false;
		if(fob.equals(type,"important") != true && fob.equals(type,"regular") != true) valid = false;
		if(fob.equals(privacy,"private") != true && fob.equals(privacy,"public") != true) valid = false;
		if(fob.validId(id)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		tags = fob.parseTag(tag); tl = tags.length;
		if(tags[0].length() == 0) {
			tags = new String[1];
			tags[0] = "general";
			tl = 1;
		}
		
		//System.out.println(heading+", "+description+", "+tag+", "+type+", "+privacy+", "+date);
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			PreparedStatement psmt, psmt1,psmt2;
			Statement smt = cn.createStatement();
			ResultSet rs;
			cn.setAutoCommit(false);
			try {
				psmt=cn.prepareStatement("insert into task (author, type, heading, description, privacy, date, datetime) values ('"+id+"', '"+type+"',?,?,'"+privacy+"','"+date+"', now())");
				psmt.setString(1,heading);
				psmt.setString(2,description);
				psmt.executeUpdate();
				psmt.close();
				rs = smt.executeQuery("select id from task where author = '"+id+"' order by datetime desc limit 1");
				rs.next();
				taskid = rs.getString("id");
				smt.executeUpdate("insert into task_user_map values ("+taskid+",'"+id+"',0,now())");
				
				psmt = cn.prepareStatement("insert into tag_task_map values(?,"+taskid+",now())");
				psmt1 = cn.prepareStatement("insert into tags values(null,'"+id+"',?,0,now())");
				psmt2 = cn.prepareStatement("insert into tag_user_map values(?,'"+id+"',now())");
				
				for(int i = 0; i < tl; i++) {
					rs = smt.executeQuery("select * from tags where tag='"+tags[i]+"' limit 1");
					if(rs.next() == false) {
						psmt1.setString(1, tags[i]);
						psmt1.executeUpdate();
					}
					rs.close();
					tagid=0;
					rs = smt.executeQuery("select * from tags where tag='"+tags[i]+"' limit 1");
					if(rs.next() == true) {
						tagid = rs.getInt("id");
						psmt.setInt(1,tagid);
						psmt.executeUpdate();
						smt.executeUpdate("update tags set used = used + 1 where tag ='"+tags[i]+"'");
					}
					rs = smt.executeQuery("select * from tag_user_map where tag_id="+tagid+" and user_id='"+id+"' limit 1");
					if(tagid > 0 && rs.next() == false) {
						psmt2.setInt(1,tagid);
						psmt2.executeUpdate();
					}
				}
				
				cn.commit();smt.close();psmt.close();psmt1.close();psmt2.close();cn.close();
			}catch(Exception e){
				cn.rollback();smt.close();cn.close();
				response.sendRedirect("Error");return;
			}
			response.sendRedirect("UserHome");return;
		}catch(Exception e){}
		
	}

}
