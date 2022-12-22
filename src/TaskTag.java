

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
 * Servlet implementation class TaskTag
 */
@WebServlet("/TaskTag")
public class TaskTag extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TaskTag() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String s="Failed",who,id,name,t,page="tasktag",tag="",imp="",task="";
		boolean valid=true;
		int count;
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions();
		if(fob.validate(ses)==false){ response.sendRedirect("Login");return;}
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		name=request.getParameter("name");
		if(fob.validTag(name)==false) valid=false;
		if(valid==false){
			request.setAttribute("error", "<span class=\"t-red\">Invalid tag name</span>");
			request.getRequestDispatcher("Error").forward(request, response);
			return;
		}
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs=smt.executeQuery("select * from task inner join tag_task_map on task.id = tag_task_map.task_id where tag_task_map.tag_id in (select id from tags where tag='"+name+"') group by task.id");
			
			s=		fob.head(id)+fob.navbar(ses)+
					"<div class=\"container\" id=\"bodycontainer\">"+
						"<div class=\"row\">"+
							"<div class=\"col-sm-3 col-md-3 col-lg-3\"> </div>"+
							"<div class=\"col-sm-6 col-md-6 col-lg-6\">"+
								"<div class=\"page-header\">"+
									"<h4 class=\"pageheading\">Search by Tag</h4>"+
								"</div>"+
								"<div id=\"maindiv\">";									
			
			task="<table class=\"table table-hover table-bordered fetchedtable\">"+
					"<tr><th>Tasks with tag</th></tr>";
			count = 0;
			while(rs.next()==true){
				t=fob.getTask(rs, page, id, who, "");
				if(t.equals("Failed")==true||t.length()==0) continue;
				imp = "";
				if(rs.getString("type").equals("important") == true) {
					imp = "imptask";
				}
				task+=
						"<tr class=\""+imp+"\">"+
							"<td>"+
								t+
							"</td>"+
						"</tr>";
				count++;
			}
			task+="</table>";
			rs.close();smt.close();cn.close();
			s+=						"<span class=\"t-blue\">"+count+" results </span></br>";
			if(count==0) s+=		"<span class=\"t-blue t-lg\"></br>No Tasks found</span>";
			else s+=				"<span id=\"tagspan\" class=\"t-blue t-lg\" style=\"display:none\"></br>No Tasks found</span>";
			s+=						task+
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
			response.sendRedirect("UserHome");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

}
