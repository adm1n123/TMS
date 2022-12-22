

import java.io.IOException;
import java.io.PrintWriter;
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
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class VerifyEmail
 */
@WebServlet("/VerifyEmail")
public class VerifyEmail extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VerifyEmail() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Functions fob=new Functions(); fob.noCache(response);
		String key,userid,email,s="",password,message;
		key=request.getParameter("key");
		email=request.getParameter("email");
		userid=request.getParameter("userid");
		
		if(key!=null&&userid!=null&&fob.validKey(key)==true&&fob.validId(userid)==true&&fob.validEmail(email)==true){
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement();
				ResultSet rs;
				cn.setAutoCommit(false);
				try{
					rs=smt.executeQuery("select * from verify_email where user_id='"+userid+"' and verify_key='"+key+"' limit 1");
					if(rs.next()==true){
						smt.executeUpdate("delete from verify_email where user_id='"+userid+"'");
						smt.executeUpdate("update users set user_email='abc@d.com' where user_email='"+email+"'");
						smt.executeUpdate("update users set user_email='"+email+"' where user_id='"+userid+"' limit 1");
						rs=smt.executeQuery("select * from users where user_id='"+userid+"' limit 1");
						if(rs.next()==true){
							password=rs.getString("user_pass");
							message="<span class=\"t-blue\"> Email has been verified successfully. <span class=\"t-grey t-sx\"> "+fob.date()+"</span></span>";
							s=fob.login(userid, password, request);
							cn.commit();rs.close();smt.close();cn.close();
							if(s.equals("User")==true){
								HashSet<String> idset = new HashSet<String>();
								idset.add(userid);
								fob.sendNotification(idset, message);
								response.sendRedirect("UserHome"); return;
							} else{
								response.sendRedirect("Login"); return;
							}
						}
					} else{
						smt.executeUpdate("delete from verify_email where verify_key='"+key+"'");
						s="<span class=\"t-red\">Invalid Link</span>";
						cn.commit();
					}
					rs.close();
					
				}catch(Exception e){}
				cn.rollback();smt.close();cn.close();
				
			}catch(Exception e){
				s="<span class=\"t-red\">Error occured please try again</span>";
			}
			request.setAttribute("error", s);
			request.getRequestDispatcher("Error").forward(request, response); return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions();
		
		String s="",who="",id="",userid,email,key,name="";
		boolean valid=true;
		
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		userid=request.getParameter("userid");
		email=request.getParameter("email");
		
		if(fob.validId(userid)==false) valid=false;
		if(fob.validEmail(email)==false) valid=false;
		if(userid.equals(id)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			smt.executeUpdate("delete from verify_email where user_id='"+userid+"'");
			do{
				key=fob.nextKey();
				rs=smt.executeQuery("select * from verify_email where verify_key='"+key+"' limit 1");
			}while(rs.next()==true);
			
			///////// sending mail ////////////
			String to,content,subject,from,password,url;
			from="info-TMS@deepakbaghel.in";
			password="N.6z7kAcAZ@s";
			to=email;
			subject="Email Verification -TMS";
			url=fob.domainName()+"/TMS/VerifyEmail?userid="+userid+"&email="+email+"&key="+key;
			content=
					"<html>"+
						"<body>"+
							"<p>Hello, <b><i>"+userid+"</i></b></p>"+
							"<p>Someone has created email verification link of your <a href=\""+fob.domainName()+"/TMS/Index\"><b>TMS</b></a> account if it was not you please ignore.</p>"+
							"<p>To verify email <a href=\""+url+"\"><button >click here</button></a></p>"+
							"<p>If unable to click please paste this link into browser's address bar and hit ENTER <a href=\""+url+"\">"+url+"</a></p>"+
							"<p><b>NOTE: This is one time email verification link.</b></p>"+
						"</body>"+
					"</html>";
			s=fob.sendMail(from, password, to, subject, content);
			if(s.equals("Success")==true){
				s="Failed";
				smt.executeUpdate("insert into verify_email values (null,'"+userid+"','"+key+"')");
				s="Success";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		pr.println(s);
		pr.flush();
	}
}
