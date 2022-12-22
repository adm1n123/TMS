

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
 * Servlet implementation class DeleteTask
 */
@WebServlet("/DeleteTask")
public class DeleteTask extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeleteTask() {
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
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions();
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String page,taskid="",Q,who="",id="",s="Failed";
		boolean valid=true;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		taskid=request.getParameter("taskid");
		page=request.getParameter("page");
		if(fob.validNumber(taskid)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return;
		
		if(who.equals("user")){
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement();
			
				Q="select * from task where id="+taskid+" and author='"+id+"' limit 1";
				ResultSet rs=smt.executeQuery(Q);
				if(rs.next()==true) who="admin"; // answer owner
				rs.close();smt.close();cn.close();
			}catch(Exception e){
				return;
			}
		}
		if(who.equals("admin")){
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement();
				PreparedStatement psmt;
				ResultSet rs;
				cn.setAutoCommit(false);
				
				try{ /////////////// moving to recycle bin //////////////
					Q="select * from task where id="+taskid+" limit 1";
					rs=smt.executeQuery(Q);
					if(rs.next()==true){
						psmt=cn.prepareStatement("insert into recycle_bin values (null,'task','"+rs.getString("id")+"','"+rs.getString("author")+"','"+rs.getString("datetime")+"',?,now())");
						psmt.setString(1,"type:"+rs.getString("type")+", heading:"+rs.getString("heading")+", privacy:"+rs.getString("privacy")+", description:"+rs.getString("description")+" date:-"+rs.getString("date"));
						psmt.executeUpdate();
						psmt.close();
					}
				}catch(Exception e){
				}
				
				try{
					
					Q="delete from task where id="+taskid+" limit 1";
					smt.executeUpdate(Q);
					Q="delete from task_user_map where task_id="+taskid;
					smt.executeUpdate(Q);
					Q="delete from tag_task_map where task_id="+taskid;
					smt.executeUpdate(Q);
					s = "Success";
					cn.commit();
				}catch(Exception e){}
				cn.rollback();smt.close();cn.close();
			}catch(Exception e){
			}
		} else{
			response.sendRedirect("Login");return;
		}
		pr.println(s);
	}
}
