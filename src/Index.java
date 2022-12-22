
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Index
 */
@WebServlet("/Index")
public class Index extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Index() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String s="",who="",id="",Home;
		PrintWriter pr=response.getWriter();
		HttpSession ses=request.getSession(false);
		Functions fob=new Functions(); fob.noCache(response);
		
		String Head="",Navbar,name="Guest",date="",st[];
		try{
			try{
				id=ses.getAttribute("user_id").toString();name=ses.getAttribute("user_fname").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();name=ses.getAttribute("admin_fname").toString();who="admin";
			}
		}catch(Exception e){}
		
		s=		"<!DOCTYPE html>" + 
				"<html lang=\"en\">" + 
					"<head>" + 
						"<title>TMS</title>"+
						"<meta charset=\"utf-8\">"+
						"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" + 
						"<link rel=\"shortcut icon\" href=\"Images/fire.gif\" type=\"image/png\" />"+
						"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css\">" + 
						"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">" +
						
						"<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script>" + // this must be a less prior than bootstrapcdn
						"<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>" + // must be after jquery.min
						
				//		"<link rel=\"stylesheet\" href=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css\">"+
				//		"<script src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js\"></script>"+
						
						"<script src=\"script.js\"></script>"+
						"<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" media=\"all\"/>"+
					"</head>" +
				"<body class=\"wcmbody\">"+
				"<div>"+fob.modal(id)+"</div>";
		
			Home= fob.navbarGuest();
					
			s+=Home;
			
			if(who.equals("user") == true) s = fob.head(id)+fob.navbarHome(ses);
			if(who.equals("admin") == true) s = fob.head(id)+fob.navbarAdmin(ses);
			
			s+=		"<div class=\"container-fluid wcmcontainer-fluid text-center\">" + 
						"<h1 class=\"margin\">Welcome</h1>" +
						"<p>We are glad to see you on TMS. This is platform to manage and schedule your tasks</p><p> and get reminder right on the day it scheduled for.</p><p>You can create your own task or just follow any public task created by community.</p>"+
						"<p>We would like to invite you, and never miss anything.</p>"+
					"</div>"+
					"<div class=\"container-fluid bg-2 text-center\">" +
						"<h3 class=\"margin\">Features</h3>" +
						"<p>You can create tasks in private/public mode and important/regular mode. You can also follow some tags</p><p> and based on your selection new tasks would be recommended not only this you may remove any task from your todo list anytime.</p>"+
						"<p> A reminder mail would be send to you along with opt-out button. </p>"
						+ "<p><span class=\"t-bold\">More Features:</span> inbox/outbox, searching user, and</p>"+
						"<p>finding the online users, account settings, searching the tasks by tag, sorting the tasks by upcoming/oldest etc.</p>"+
					"</div>"+
					"<div class=\"container-fluid text-center\">" +
						"<h3 class=\"margin\">About</h3>" +
						"<p>This web application is developed by students of <a href=\"http://mitsgwalior.in/\" target=\"_blank\">Madhav Institute of Technology and Science, Gwalior</a> </p><p> during Bachelor of Engineering as Major Project.</p>"+
					"</div>"+
					"<div class=\"container text-center\">" +
						"<table class=\"table table-bordered\">"+
							"<tr>"+
								"<td>"+
									"<img src=\"https://www.gravatar.com/avatar/"+fob.md5Hex("panditaditya60@gmail.com")+"?s=150.jpg\" class=\"img-responsive img-circle margin admin123\" alt=\"Author\"> <span class=\"t-bold t-grey\">Aditya</span>"+
								"</td>"+
								"<td>"+
									"<img src=\"https://www.gravatar.com/avatar/"+fob.md5Hex("campioknight@gmail.com")+"?s=150.jpg\" class=\"img-responsive img-circle margin admin123\" alt=\"Author\"> <span class=\"t-bold t-grey\">Anshul</span>"+
								"</td>"+
								"<td>"+
									"<img src=\"https://www.gravatar.com/avatar/"+fob.md5Hex("deepakbaghel31@gmail.com")+"?s=150.jpg\" class=\"img-responsive img-circle margin admin123\" alt=\"Author\"> <span class=\"t-bold t-grey\">Deepak</span>"+
								"</td>"+
								"<td>"+
									"<img src=\"https://www.gravatar.com/avatar/"+fob.md5Hex("pragammgandhi@gmail.com")+"?s=150.jpg\" class=\"img-responsive img-circle margin admin123\" alt=\"Author\"> <span class=\"t-bold t-grey\">Pragam</span>"+
								"</td>"+
							"</tr>"+
						"</table>"+
					"</div>"+
					"<div class=\"container-fluid text-center\">" +
						"<p><span class=\"t-bold\">Technology: </span> Backend is developed in Java servlet with MySQL as database engine </p><p>and frontend involves HTML, CSS, bootstrap, javascript, jquery.</p></br>"+
						"<p>If you find any bug then please let us know any suggestions/improvements are welcome.</p>"+
					"</div>";
			s+= 	fob.footer()+
				"</body>"+
			"<html>";
		
		pr.println(s);
		
	}

}
