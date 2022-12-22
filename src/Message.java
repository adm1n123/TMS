

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
 * Servlet implementation class Message
 */
@WebServlet("/Message")
public class Message extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Message() {
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
		
		////////////////////////// Home ///////////////////////////////////////////////
		String id="",s="",userid,type,heading,placeholder;
		boolean valid=true;
		String Head,Navbar,who;
		
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}

		type=request.getParameter("type");
		
		if(fob.validName(type)==false) valid=false;
		if(valid==false) { response.sendRedirect("Error");return;}
		
		s=fob.head(id);
		if(who.equals("user")==true) s+=fob.navbarHome(ses);
		else s+=fob.navbarAdmin(ses);
		
		if(type.equals("feedback")==true){
			heading="Feedback";placeholder="you feedback is valuable";
		} else if(type.equals("bug")==true){
			heading="Report bug";placeholder="please mention bug type and details etc.";
		} else if(type.equals("feature")==true){
			heading="Request feature";placeholder="how can we improve";
		} else if(type.equals("help")==true){
			heading="Help";placeholder="how can we help you?";
		}  else if(type.equals("contact")==true){
			heading="Contact us";placeholder="enter your contact details we shall reach you";
		} else {
			response.sendRedirect("UserHome");return;
		}
		
				s+=  // sending message to admin123
						"<div class=\"container\" id=\"bodycontainer\">"+
						"<div class=\"row\">"+
							"<div class=\"col-md-6 col-md-offset-3\">"+
								"<div class=\"page-header\">"+
									"<h4 class=\"pageheading\">"+heading+"</h4>"+
								"</div>"+
								"<div>"+
									"<form action=\"Message\" method=\"post\">"+
										"<span id=\"sendadminmessagespan\"></span></br>";
										
				if(type.equals("contact")) s+=	"<input class=\"form-control\" id=\"sendinput\" required cols=\"100\" name=\"email\" placeholder=\"Enter your email\"/></br></br>";
				s+=						"<textarea class=\"form-control\" id=\"sendtextarea\" required rows=\"15\" cols=\"100\" name=\"message\" onkeyup=findAdminMessageLength(); placeholder=\""+placeholder+"\"></textarea>"+
										"<div class=\"b-save\"><button class=\"btn btn-success\" type=\"submit\" id=\"sendbutton\" name=\"button\" value=\""+type+"\"><i class=\"glyphicon glyphicon-send\"></i> Send</button></div>"+
									"</form>"+
								"</div>"+
							"</div>"+
						"</div>"+
					"</div>"+
					fob.footer()+
				"</body>"+
			"</html>";
		pr.println(s);
		pr.flush();
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions(); fob.noCache(response);
		
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String s="Failed",id="",userid,type,message,email;
		boolean valid=true;
		String who="";
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}
		
		type=request.getParameter("button");
		message=request.getParameter("message");
		email = request.getParameter("email");
		
		if(fob.validName(type)==false) valid=false;
		if(fob.validText(message)==false) valid=false;
		if(fob.equals(type,"contact") == true) {
			if(fob.validEmail(email) == false) valid = false;
		}
		if(valid==false) { response.sendRedirect("Error");return;}
		if(message.length()>5000) message=message.substring(0, 5000);
		
		if(type.equals("contact") == true) message = "email: "+email+" message: "+message;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			PreparedStatement psmt;
			psmt=cn.prepareStatement("insert into admin_messages values (null,'"+type+"','"+id+"',?,now())");
			psmt.setString(1, message);
			psmt.executeUpdate();
			psmt.close();cn.close();
			message = "<span class=\"t-blue\">We have received your message <span class=\"t-grey t-sx\">"+fob.date()+"</span></span>";
			HashSet<String> idset = new HashSet<String>();
			idset.add(id);
			fob.sendNotification(idset, message);
			response.sendRedirect("UserHome");
		}catch(Exception e){}
		
		pr.println(s);
	}
}
