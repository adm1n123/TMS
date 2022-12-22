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
 * Servlet implementation class Community
 */
@WebServlet("/Community")
public class Community extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Community() {
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
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String recent="",t,Head,Navbar,who,page="community",userid,s;
		String id="",Q;
		int count=0;
		boolean valid=true;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		if(who.equals("user")==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		Head=fob.head(id);
		Navbar=fob.navbar(ses);
		s=Head+Navbar;
		
		
		////////////////////////////////////  recent asked questions /////////////////////////////////
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			s+=		"<div class=\"container\" id=\"bodycontainer\">"+
						"<div class=\"row\">"+
							"<div class=\"col-md-3\">"+
								"<div class=\"page-header\">"+
									//"<span class=\"pageheading\">Welcome on TMS <a href=\"Profile?id="+id+"\">"+id+"</a></span>"+
								"</div>"+
								//"<div>Last login <span class=\"t-grey\">"+ses.getAttribute("Time")+"</span></div></br></br></br>"+
								"<div style=\"overflow:auto;\">"+
									"<table class=\"table table-hover table-bordered fetchedtable\">"+
										"<tr><th>Online users</th></tr>";
			
			rs=smt.executeQuery("select user_id,user_fname,user_lname from active_users order by datetime desc");
			while(rs.next()==true){
				userid=rs.getString("user_id");if(userid.length()>10) userid=userid.substring(0, 10);
				s+=						"<tr><td><a href=\"Profile?id="+rs.getString("user_id")+"\">"+rs.getString("user_fname")+" "+rs.getString("user_lname")+"</a></td></tr>";
			}
			s+=						"</table>"+
								"</div>"+
								"<div id=\"tagdiv\" style=\"overflow:auto;\">"+fob.recentTags(id,page)+"</div>"+
							"</div>"+
							"<div class=\"col-md-6\">"+
								"<div class=\"page-header\">"+
									"<h4 class=\"pageheading\">Recent activity</h4>"+
								"</div>"+
								"<div id=\"maindiv\">";
			Q="select * from questions order by datetime desc";
			rs=smt.executeQuery(Q);
			count=0;
			while(rs.next()){
				t=fob.question(id, page, rs);
				if(t.equals("Failed")==true||t.length()==0) continue;
				recent+=			"<table id=\"recenttable\" class=\"table table-hover\">"+
										"<tr>"+
											"<td>"+
												t+
											"</td>"+
										"</tr>"+
									"</table>";
				count++;
			}
			if(count==0) s+=		"<span class=\"t-blue t-lg\"></br>No recent activity found</span>";
			else s+=				"<span id=\"recentspan\" class=\"t-blue t-lg\" style=\"display:none\"></br>No recent activity found</span>";
			s+=						recent+
								"</div>"+
							"</div>"+
						"</div>"+
					"</div>"+
					fob.footer()+
				"</body>"+
			"</html>";
			pr.println(s);
			pr.flush();
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			pr.println();
		}
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
