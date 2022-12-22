

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
 * Servlet implementation class MapUser
 */
@WebServlet("/MapUser")
public class MapUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MapUser() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions();
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String id="",s="Failed",tab,page,who,type,typeid,operation;
		boolean valid=true;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}

		type=request.getParameter("type");
		typeid=request.getParameter("typeid");
		operation=request.getParameter("operation");
	
		if(fob.equals(type,"tag") == false && fob.equals(type, "task") == false) valid=false;
		if(fob.validNumber(typeid)==false) valid=false;
		if(fob.validName(operation)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		// one can not opt out his own task(private/public) without deleting
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt = cn.createStatement();
			ResultSet rs;
			
			rs = smt.executeQuery("select * from "+type+"_user_map where "+type+"_id="+typeid+" and user_id='"+id+"' limit 1");
			if(rs.next() == false && fob.equals(operation, "remove") == false) {
				if(type.equals("tag")) smt.executeUpdate("insert into tag_user_map values("+typeid+",'"+id+"',now())");
				if(type.equals("task")) smt.executeUpdate("insert into task_user_map values("+typeid+",'"+id+"',0,now())");
				
				s = "Success 1";
			} else {
				if(smt.executeUpdate("delete from "+type+"_user_map where "+type+"_id="+typeid+" and user_id='"+id+"'") == 1 ) {
					s = "Success 0";
				}
			}
		}catch(Exception e) {
			
		}
		
		pr.println(s);
		pr.flush();
	}

}
