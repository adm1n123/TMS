

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class TaskEdit
 */
@WebServlet("/TaskEdit")
public class TaskEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TaskEdit() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String s="",Q,id="",taskid,fname,lname,gender,email,about,male="",female="",password,countries="",country,heading,description,date,tag,type,important,regular,privacy,pub,pri;
		boolean valid=true;
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions(); fob.noCache(response);
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		
		String Head,Navbar,who="";
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		taskid = request.getParameter("taskid");
		if(fob.validId(id)==false) valid=false;
		if(fob.validNumber(taskid)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		Head=fob.head(id);
		Navbar=fob.navbarHome(ses);
		s=Head+Navbar;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			Q="select * from task where id='"+taskid+"' and author = '"+id+"' limit 1";
			ResultSet rs=smt.executeQuery(Q);
			if(rs.next() == true || who.equals("admin") == true){
				heading=rs.getString("heading");
				description=rs.getString("description");
				date=rs.getString("date");
				type=rs.getString("type");
				privacy=rs.getString("privacy");

				pub = ""; pri = ""; important = ""; regular = ""; tag = "";
				if(type.equals("important") == true) important = "checked"; else regular = "checked";
				if(privacy.equals("private") == true) pri = "checked"; else pub = "checked";
				date = dateRev(date);
				rs=smt.executeQuery("select tag from tags where id in (select tag_id from tag_task_map where task_id = "+taskid+")");
				while(rs.next()){
					tag += rs.getString("tag")+" ";
				}
				
				s+=
							"<div class=\"container\" id=\"bodycontainer\">"+
								"<div class=\"row\">"+
									"<div class=\"col-md-6 col-md-offset-3\">"+
										"<div class=\"page-header\">"+
											"<h4 class=\"pageheading\">Modify Task</h4>"+
										"</div>"+
										"<div class=\"panel panel-info\" id=\"addtaskpanel\">"+
											"<div class=\"panel-heading\">Modify Panel</div>"+
											"<div style=\"padding-top:40px\" class=\"panel-body\">"+
												"<form action=\"TaskEdit\" method=\"post\">"+
													"<input type=\"text\" class=\"display-n\" name=\"taskid\" value=\""+taskid+"\">"+
													"<div class=\"form-group\">"+
														"<label for=\"heading\">Task:</label>"+
														"<input class=\"form-control\" type=\"text\" id=\"heading\" name=\"heading\" maxlength=\"100\" required placeholder=\"type task heading\" value=\""+heading+"\">"+
													"</div>"+
													"<div class=\"form-group\">"+
														"<label for=\"description\">Description:</label>"+
														"<input class=\"form-control\" type=\"text\" id=\"description\" name=\"description\" maxlength=\"500\" placeholder=\"description about task\" value=\""+description+"\">"+
													"</div>"+
													"<div class=\"form-group\">"+
														"<label for=\"Type\">Type:</label>"+
														"<div class=\"radio\">"+
															"<label class=\"radio-inline\"><input type=\"radio\" name=\"type\" value=\"important\" "+important+"> Important</label>"+
															"<label class=\"radio-inline\"><input type=\"radio\" name=\"type\" value=\"regular\" "+regular+"> Regular</label>"+
														"</div>"+
													"</div>"+
													"<div class=\"form-group\">"+
														"<label for=\"privacy\">Privacy:</label>"+
														"<div class=\"radio\">"+
															"<label class=\"radio-inline\"><input type=\"radio\" name=\"privacy\" value=\"private\" "+pri+"> Private</label>"+
															"<label class=\"radio-inline\"><input type=\"radio\" name=\"privacy\" value=\"public\" "+pub+"> Public</label>"+
														"</div>"+
													"</div>"+
													"<div class=\"form-group\">"+
														"<label for=\"date\">Date:</label>"+
														"<input class=\"form-control\" type=\"date\" id=\"date\" name=\"date\" required placeholder=\"dd/mm/yyyy\" value=\""+date+"\">"+
													"</div>"+
													"<div class=\"form-group\">"+
														"<label for=\"tags\">tags:</label>"+
														"<input class=\"form-control\" type=\"text\" id=\"tags\" name=\"tag\" required placeholder=\"tags separated by space\"  value=\""+tag+"\">"+
													"</div>"+
													"<button class=\"btn btn-success f-right\" type=\"submit\"><i class=\"fa fa-save\"></i> Save</button>"+
												"</form>"+
											"</div>"+
										"</div>"+
									"</div>"+
								"</div>"+
							"</div>"+
							fob.footer()+
						"</body>"+
					"</html>";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
		//	System.out.println(e);
		}
		pr.println(s);
		pr.flush();
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions(); fob.noCache(response);
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String remainder="",t,Head,fname="",lname="",Navbar,who,userid,s, heading, description, tags[],oldtags[] = new String[100], tag, type, privacy,date, taskid, notification;
		String id="",Q;
		int count=0, tl=0,otl=0, tagid;
		boolean valid=true;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
				fname=ses.getAttribute("user_fname").toString();
				lname=ses.getAttribute("user_lname").toString();
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
				fname=ses.getAttribute("admin_fname").toString();
				lname=ses.getAttribute("admin_lname").toString();
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		taskid = request.getParameter("taskid");
		heading = request.getParameter("heading").trim();
		description = request.getParameter("description").trim();
		tag = request.getParameter("tag");
		type = request.getParameter("type");
		privacy = request.getParameter("privacy");
		date = fob.parseDate(request.getParameter("date"));
		
		if(heading.length() > 200) heading = heading.substring(0, 200);
		if(description.length() > 500) description = description.substring(0, 500);
		
		if(fob.validNumber(taskid) == false) valid = false;
		if(heading.length() == 0 || date.length() == 0) valid = false;
		if(fob.equals(type,"important") != true && fob.equals(type,"regular") != true) valid = false;
		if(fob.equals(privacy,"private") != true && fob.equals(privacy,"public") != true) valid = false;
		if(fob.validId(id)==false) valid=false;
		if(valid==false) { response.sendRedirect("TaskEdit?taskid="+taskid);return;}

		tags = fob.parseTag(tag); tl = tags.length;
		if(tags[0].length() == 0) {
			tags = new String[1];
			tags[0] = "general";
			tl = 1;
		}
		
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			PreparedStatement psmt, psmt1,psmt2;
			Statement smt = cn.createStatement();
			ResultSet rs;

			rs = smt.executeQuery("select * from task where id = "+taskid+" and author = '"+id+"'");
			if(rs.next() == false) {
				response.sendRedirect("Error");return;
			}
			cn.setAutoCommit(false);
			try {
				psmt=cn.prepareStatement("update task set type='"+type+"', heading = ?, description = ?, privacy = '"+privacy+"', date = '"+date+"' where id = "+taskid);
				psmt.setString(1,heading);
				psmt.setString(2,description);
				psmt.executeUpdate();
				psmt.close();
				
				// resolve old new tag issue
				rs = smt.executeQuery("select tag from tags where id in (select tag_id from tag_task_map where task_id = "+taskid+")");
				while(rs.next() == true) {
					oldtags[otl++] = rs.getString("tag");
				}
				Arrays.sort(oldtags, 0, otl);
				Arrays.sort(tags, 0, tl);
				int i = 0, j = 0;
				while(i < otl && j < tl) {
					if(oldtags[i].equals(tags[j]) == true) {
						oldtags[i] = tags[j] = "";
						i += 1; j += 1;
					} else if(oldtags[i].compareTo(tags[j]) > 0) {
						j += 1;
					} else {
						i += 1;
					}
				}

				for(i = 0; i < otl; i++) {
					if(oldtags[i].length() == 0) continue;
					smt.executeUpdate("delete from tag_task_map where task_id = "+taskid+" and tag_id in (select id from tags where tag='"+oldtags[i]+"')");
				}

				// insert new tags for task
				psmt = cn.prepareStatement("insert into tag_task_map values(?,"+taskid+",now())");
				psmt1 = cn.prepareStatement("insert into tags values(null,'"+id+"',?,0,now())");
				psmt2 = cn.prepareStatement("insert into tag_user_map values(?,'"+id+"',now())");
				
				for(i = 0; i < tl; i++) {
					if(tags[i].length() == 0) continue;
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
				
				cn.commit();psmt.close();psmt1.close();psmt2.close();

				/////// sending notification to followers/////////////
				HashSet<String> idset=new HashSet<String>();
				date=fob.date();
				notification="<span><a href=\"Profile?id="+id+"\">"+fname+" "+lname+"</a> edited his <a href=\"Task?id="+taskid+"\">Task</a> you are following. <span class=\"t-grey t-sx\"> "+date+"</span></span>";
				Q="select user_id from task_user_map where task_id="+taskid;
				rs=smt.executeQuery(Q);
				while(rs.next()==true) idset.add(rs.getString("user_id"));
				idset.remove(id);
				fob.sendNotification(idset, notification);
				idset.clear();
				rs.close();smt.close();cn.close();
			}catch(Exception e){
				cn.rollback();smt.close();cn.close();
				response.sendRedirect("Error");return;
			}
			response.sendRedirect("UserHome");return;
		}catch(Exception e){}
		
	}

	
	
	
	
	protected String dateRev(String date) {
		for(int i = 0; i < date.length(); i++) {
			char c = date.charAt(i);
			if(c > '9' || c < '0') {
				String t,d[] = date.split(c+"");
				t = d[0]; 
				d[0] = d[2];
				d[2] = t;
				return d[0]+c+d[1]+c+d[2];
			}
		}
		return date;
	}
}
