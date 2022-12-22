

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RemoveReminder
 */
@WebServlet("/RemoveReminder")
public class RemoveReminder extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveReminder() {
        super();
        // TODO Auto-generated constructor stub
    }
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Functions fob=new Functions(); fob.noCache(response);
		String key,userid,email,s="Failed";
		int taskid;
		boolean valid=true;
		taskid=Integer.parseInt(request.getParameter("taskid"));
		key=request.getParameter("key");
		email=request.getParameter("email");
		userid=request.getParameter("userid");
		if(fob.validId(userid) == false) valid = false;
		if(fob.validEmail(email) == false) valid = false;
		if(fob.validKey(key) == false) valid = false;

		if(valid == true) {
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement();
				ResultSet rs;
				smt.executeUpdate("delete from remove_reminder where datetime < date_sub(now(), interval 1 day)");
				cn.setAutoCommit(false);
				try{
					rs=smt.executeQuery("select * from remove_reminder where user_id='"+userid+"' and verify_key='"+key+"' and task_id="+taskid+" and user_email = '"+email+"' limit 1");
					if(rs.next()==true){
						smt.executeUpdate("delete from remove_reminder where user_id='"+userid+"'");
						smt.executeUpdate("delete from task_user_map where task_id="+taskid+" and user_id='"+userid+"'");
						s="<span class=\"t-green\">Successfully removed</span>";
					} else{
						smt.executeUpdate("delete from remove_reminder where verify_key='"+key+"'");
						s="<span class=\"t-red\">Invalid Link</span>";
					}
					cn.commit();rs.close();
				}catch(Exception e){}
				
				cn.rollback();smt.close();cn.close();
			}catch(Exception e){
				s="<span class=\"t-red\">Error occured please try again</span>";
			}
		}
		request.setAttribute("error", s);
		request.getRequestDispatcher("Error").forward(request, response); return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

}
