

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class AdminPanel
 */
@WebServlet("/AdminPanel")
public class AdminPanel extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminPanel() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		
		
		/////// Fake admin panel //////////////
		/////// Fake admin panel //////////////
		/////// Fake admin panel //////////////
		
		
		
		String s, buttons, Home, id = "admin", pass, notificationcount = "1";
		HttpSession fake = request.getSession(false);
		Functions fob = new Functions();
		PrintWriter pr = response.getWriter();
		try{
			id = fake.getAttribute("id").toString();
			pass = fake.getAttribute("pass").toString();
		}catch(Exception e){
			response.sendRedirect("Login");return;
		}
		fake.removeAttribute("id");
		fake.removeAttribute("pass");
		fake.invalidate();
		
		notificationcount="<sup><span id=\"notificationcount\" class=\"label\">"+notificationcount+"</span></sup>";
		
		buttons=
				"<ul class=\"dropdown-menu menulink\">"+
						"<li><a href=\"Profile?id=admin123\">Profile</a></li>"+
						"<li><a href=\"AskedQuestions\">Asked Questions</a></li>"+
						"<li><a href=\"MyAnswers\">My Answers</a></li>"+
						"<li><a href=\"ReadingList\">Reading List</a></li>"+
						"<li><a href=\"Inbox\">Inbox</a></li>"+
						"<li><a href=\"Outbox\">Outbox</a></li>"+
						"<li><a href=\"AdminAccountSetting\">Account settings</a></li>"+
						"<li><a href=\"SearchUser\">Search User</a></li>"+
						"<li><a href=\"Notes\">My Notes</a></li>"+
						"<li><a href=\"Suspicious\">Suspicious Activity</a></li>"+
						"<li><a href=\"DeleteUser\">Delete User</a></li>"+
						"<li><a href=\"DeleteQuestion\">Delete Question</a></li>"+
						"<li><a href=\"DeleteAnswer\">Delete Answer</a></li>"+
						"<li><a href=\"DeleteTag\">Delete Tag</a></li>"+
					//	"<li><a href=\"RecycleBin\">Recycle Bin</a></li>"+
						"<li><a href=\"AdminMessage\">Messages</a></li>"+
						"<li><a href=\"Logout\"><i class=\"fa fa-sign-out fa-lg\"></i> Logout</a></li>"+
					"</ul>";
		
		Home=
					"<nav class=\"navbar navbar-default navbar-fixed-top\" role=\"navigation\">"+
						"<div class=\"container\">"+
							"<div class=\"navbar-header\">"+
								"<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#mynavbar\">"+
									"<span class=\"sr-only\">Toggle navigation</span>"+	
									"<span class=\"icon-bar\"></span>"+
									"<span class=\"icon-bar\"></span>"+
									"<span class=\"icon-bar\"></span>"+
								"</button>"+
								"<span class=\"navbar-brand\"><a href=\"Index\"><img class=\"img-responsive\" src=\"Images/TMS_logo.png\" alt=\"TMS\" id=\"sitelogo\" /></a></span>"+
							"</div>"+
							"<div class=\"collapse navbar-collapse\" id=\"mynavbar\">"+
								"<ul class=\"nav navbar-nav navbar-left\">"+
									"<li><a href=\"AdminHome\"><i class=\"fa fa-home fa-lg\"></i> Home</a></li>"+
									"<li>"+
										"<form class=\"navbar-form\" action=\"SearchOrAskQuestion\" method=\"post\">"+
											"<div class=\"input-group\">"+
												"<input type=\"text\" class=\"form-control\" size=\"55\" maxlength=\"200\" name=\"question\" required=\"required\" onfocus=modal(); pattern=\".{10,200}\" title=\"only 10-200 characters\" placeholder=\" Search or Ask query here\">"+
												"<div class=\"input-group-btn\">"+
													"<button class=\"btn btn-info\" name=\"searchquestion\" value=\"search\"><i class=\"glyphicon glyphicon-search\"></i> Search</button>"+
													"<button class=\"btn btn-default\" name=\"askquestion\" value=\"ask\">Ask</button>"+
												"</div>"+
											"</div>"+
										"</form>"+
									"</li>"+
								"</ul>"+
								"<ul class=\"nav navbar-nav navbar-right\">"+
									"<li class=\"dropdown\">"+
										"<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">Hi &nbsp;<span id=\"username\">Admin</span> <span class=\"caret\"></span></a>"+ 
										buttons+
									"</li>"+
									"<li>"+
										"<a href=\"#\" data-toggle=\"popover\" data-trigger=\"click\" data-html=\"true\" data-placement=\"bottom\" data-content='<div id=\"notificationdiv\"><div class=\"t-center\"><i class=\"fa fa-spinner fa-pulse fa-2x t-blue\"></i></div></div>' onclick=notification(\"show\",\""+id+"\",\"1\");> <span><i class=\"fa fa-bell-o fa-lg\"></i>"+notificationcount+" Notifications</span> </a>"+
									"</li>"+
								"</ul>"+
							"</div>"+
						"</div>"+
					"</nav>";
		
		s = fob.head("admin")+Home;
		s+=
					"<div class=\"container\" id=\"bodycontainer\">"+
						"<div class=\"row\">"+
							"<div class=\"col-md-6 col-md-offset-3\">"+
								"<div class=\"page-header\">"+
									"<h4 class=\"pageheading\">Welcome back <span class=\"t-blue t-lg\">admin</span></h4>"+
								"</div>"+
								"<div>"+
									"<p> Some suspicious activity has been recorded. Someone tried to hack into the system</p></br>"+
									"<p>Last login attempt failed with ID: <span class=\"t-red\"> "+id+"</span> Password: <span class=\"t-red\">"+pass+"</span></p>"+
									"<p>and probably that hacker is redirected to this place :)</p>"+
								"</div>"+
							"</div>"+
						"</div>"+
					"</div>";
		pr.println(s);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
