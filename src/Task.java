

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Task
 */
@WebServlet("/Task")
public class Task extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Task() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions(); fob.noCache(response);
		if(fob.validate(ses)==false){ response.sendRedirect("Login");return;}
		/////////// admin can look at this for suspicious activity ////////
		String s="",who="",id="",page="task",t,taskid,imp;
		int count = 0;
		boolean valid=true;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		taskid=request.getParameter("id");
		if(fob.validId(id)==false) valid=false;
		if(fob.validNumber(taskid)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		if(who.equals("user")) s=fob.head(id)+fob.navbarHome(ses);
		else if(who.equals("admin")==true) s=fob.head(id)+fob.navbarAdmin(ses);
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			if(who.equals("user")) s=fob.head(id)+fob.navbar(ses);
			else if(who.equals("admin")==true) s=fob.head(id)+fob.navbarAdmin(ses);
			s+=
					"<div class=\"container\" id=\"bodycontainer\">"+
						"<div class=\"row\">"+
							"<div class=\"col-md-6 col-md-offset-3\">"+
								"<div class=\"page-header\">"+
									"<h4 class=\"pageheading\">Task</h4>"+
								"</div>"+
								"<div id=\"maindiv\">";
			
			rs=smt.executeQuery("select * from task where id="+taskid);
			s+=						"<table class=\"table table-hover table-bordered fetchedtable\">";
			
			while(rs.next()==true){
				t=fob.getTask(rs, page, id, who, "task");
				if(t.equals("Failed")==true||t.length()==0) continue;
				imp = "";
				if(rs.getString("type").equals("important") == true) {
					imp = "imptask";
				}
				s+=
										"<tr class=\""+imp+"\">"+
											"<td>"+
												t+
											"</td>"+
										"</tr>";
				count++;
			}
			s+=						"</table>";
			if(count == 0) s = 		"<span class=\"t-large t-red\">Task Not Found</span>";					
			s+=					"</div>"+
							"</div>"+
						"</div>"+
					"</div>"+
					fob.footer()+
				"</body>"+
			"</html>";
			pr.println(s);
			rs.close();smt.close();cn.close();
			
		}catch(Exception e){
			s="Failed";
		}
		pr.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
