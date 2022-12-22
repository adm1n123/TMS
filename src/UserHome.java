

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
 * Servlet implementation class UserHome
 */
@WebServlet("/UserHome")
public class UserHome extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserHome() {
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
		Functions fob=new Functions(); //fob.noCache(response);
		if(fob.validate(ses)==false) { response.sendRedirect("Login");return;}
		
		String remainder="",t,Head,Navbar,who,page="userhome",userid,s,taskcount;
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
		Navbar=fob.navbarHome(ses);
		s=Head+Navbar;
		////////////////////////////// clock ///////////////////////////////////////////
		String clock=
			"<canvas id=\"canvas\"></canvas>"+ 
			"<script>\r\n" + 
			"var canvas = document.getElementById(\"canvas\");\r\n" + 
			"var ctx = canvas.getContext(\"2d\");" + 
			"var radius = canvas.height / 2;" + 
			"ctx.translate(radius, radius);\r\n" + 
			"radius = radius * 0.90\r\n" + 
			"ctx.textAlign = 'center';"+
			"setInterval(drawClock, 1000);\r\n" + 
			"\r\n" + 
			"function drawClock() {\r\n" + 
			"  drawFace(ctx, radius);\r\n" + 
			"  drawNumbers(ctx, radius);\r\n" + 
			"  drawTime(ctx, radius);\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"function drawFace(ctx, radius) {\r\n" + 
			"  var grad;\r\n" + 
			"  ctx.beginPath();\r\n" + 
			"  ctx.arc(0, 0, radius, 0, 2*Math.PI);\r\n" + 
			"  ctx.fillStyle = 'white';\r\n" + 
			"  ctx.fill();\r\n" + 
			"  grad = ctx.createRadialGradient(0,0,radius*0.95, 0,0,radius*1.05);\r\n" + 
			"  grad.addColorStop(0, '#333');\r\n" + 
			"  grad.addColorStop(0.5, 'darkgrey');\r\n" + 
			"  grad.addColorStop(1, '#333');\r\n" + 
			"  ctx.strokeStyle = grad;\r\n" + 
			"  ctx.lineWidth = radius*0.1;\r\n" + 
			"  ctx.stroke();\r\n" + 
			"  ctx.beginPath();\r\n" + 
			"  ctx.arc(0, 0, radius*0.1, 0, 2*Math.PI);\r\n" + 
			"  ctx.fillStyle = '#333';\r\n" + 
			"  ctx.fill();\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"function drawNumbers(ctx, radius) {\r\n" + 
			"  var ang;\r\n" + 
			"  var num;\r\n" + 
			"  ctx.font = radius*0.15 + \"px arial\";\r\n" + 
			"  ctx.textBaseline=\"middle\";\r\n" + 
			"  ctx.textAlign=\"center\";\r\n" + 
			"  for(num = 1; num < 13; num++){\r\n" + 
			"    ang = num * Math.PI / 6;\r\n" + 
			"    ctx.rotate(ang);\r\n" + 
			"    ctx.translate(0, -radius*0.85);\r\n" + 
			"    ctx.rotate(-ang);\r\n" + 
			"    ctx.fillText(num.toString(), 0, 0);\r\n" + 
			"    ctx.rotate(ang);\r\n" + 
			"    ctx.translate(0, radius*0.85);\r\n" + 
			"    ctx.rotate(-ang);\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"function drawTime(ctx, radius){\r\n" + 
			"    var now = new Date();\r\n" + 
			"    var hour = now.getHours();\r\n" + 
			"    var minute = now.getMinutes();\r\n" + 
			"    var second = now.getSeconds();\r\n" + 
			"    //hour\r\n" + 
			"    hour=hour%12;\r\n" + 
			"    hour=(hour*Math.PI/6)+\r\n" + 
			"    (minute*Math.PI/(6*60))+\r\n" + 
			"    (second*Math.PI/(360*60));\r\n" + 
			"    drawHand(ctx, hour, radius*0.5, radius*0.07);\r\n" + 
			"    //minute\r\n" + 
			"    minute=(minute*Math.PI/30)+(second*Math.PI/(30*60));\r\n" + 
			"    drawHand(ctx, minute, radius*0.8, radius*0.07);\r\n" + 
			"    // second\r\n" + 
			"    second=(second*Math.PI/30);\r\n" + 
			"    drawHand(ctx, second, radius*0.9, radius*0.02);\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"function drawHand(ctx, pos, length, width) {\r\n" + 
			"    ctx.beginPath();\r\n" + 
			"    ctx.lineWidth = width;\r\n" + 
			"    ctx.lineCap = \"round\";\r\n" + 
			"    ctx.moveTo(0,0);\r\n" + 
			"    ctx.rotate(pos);\r\n" + 
			"    ctx.lineTo(0, -length);\r\n" + 
			"    ctx.stroke();\r\n" + 
			"    ctx.rotate(-pos);\r\n" + 
			"}\r\n" + 
			"</script>";
		
		////////////////////////////////////  remainder /////////////////////////////////
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			Q = "select count(id) as countid from task where (date >= curdate() and id in (select task_id from task_user_map where user_id = '"+id+"'))"; // check for the server time it may vary
			ResultSet rs = smt.executeQuery(Q);
			rs.next();
			taskcount = rs.getString("countid");
			
			s+=		"<div class=\"container\" id=\"bodycontainer\">"+
						"<div class=\"row\">"+
							"<div class=\"col-md-3\">"+
								"<div class=\"page-header\">"+
									"<span class=\"pageheading\">Welcome on TMS <a href=\"Profile?id="+id+"\">"+id+"</a></span>"+
								"</div>"+
								"<div>Last login <span class=\"t-grey\">"+ses.getAttribute("Time")+"</span></div></br>"+
								"<div>"+clock+"</div><br>"+
								"<div id=\"tagdiv\" style=\"overflow:auto;\">"+fob.recentTags(id,page)+"</div>"+
							"</div>"+
							"<div class=\"col-md-6\">"+
								"<div class=\"page-header\">"+
									"<span class=\"f-right\"><a href=\"javascript:void(0)\" id=\"createtask\" onclick=addTask();><i class=\"fa fa-plus-circle i-green fa-3x\"></i></a></span>"+
									"<h4 class=\"pageheading\">My tasks</h4>"+
									"<div class=\"clearfix\"></div>"+
								"</div>"+
								"<div id=\"maindiv\">"+
								/// add task panel /////////////////////////
									"<div class=\"panel panel-info display-n\" id=\"addtaskpanel\">"+
										"<div class=\"panel-heading\">New task</div>"+
										"<div style=\"padding-top:40px\" class=\"panel-body\">"+
											"<form action=\"AddTask\" method=\"post\">"+
												"<div class=\"form-group\">"+
													"<label for=\"heading\">Task:</label>"+
													"<input class=\"form-control\" type=\"text\" id=\"heading\" name=\"heading\" maxlength=\"100\" required placeholder=\"type task heading\">"+
												"</div>"+
												"<div class=\"form-group\">"+
													"<label for=\"description\">Description:</label>"+
													"<input class=\"form-control\" type=\"text\" id=\"description\" name=\"description\" maxlength=\"500\" placeholder=\"description about task\">"+
												"</div>"+
												"<div class=\"form-group\">"+
													"<label for=\"Type\">Type:</label>"+
													"<div class=\"radio\">"+
														"<label class=\"radio-inline\"><input type=\"radio\" name=\"type\" value=\"important\" checked> Important</label>"+
														"<label class=\"radio-inline\"><input type=\"radio\" name=\"type\" value=\"regular\"> Regular</label>"+
													"</div>"+
												"</div>"+
												"<div class=\"form-group\">"+
													"<label for=\"privacy\">Privacy:</label>"+
													"<div class=\"radio\">"+
														"<label class=\"radio-inline\"><input type=\"radio\" name=\"privacy\" value=\"private\" checked> Private</label>"+
														"<label class=\"radio-inline\"><input type=\"radio\" name=\"privacy\" value=\"public\"> Public</label>"+
													"</div>"+
												"</div>"+
												"<div class=\"form-group\">"+
													"<label for=\"date\">Date:</label>"+
													"<input class=\"form-control\" type=\"date\" id=\"date\" name=\"date\" required placeholder=\"dd/mm/yyyy\">"+
												"</div>"+
												"<div class=\"form-group\">"+
													"<label for=\"tags\">tags:</label>"+
													"<input class=\"form-control\" type=\"text\" id=\"tags\" name=\"tag\" required placeholder=\"tags separated by space\">"+
												"</div>"+
												"<button class=\"btn btn-success f-right\" type=\"submit\"><i class=\"fa fa-plus\"></i> Add</button>"+
											"</form>"+
										"</div>"+
									"</div>"+
							/// sort by tab /////////////////////////////////////////			
									"<ul class=\"nav nav-tabs sortbytab\">" + 
										"<li class=\"active\"><a data-toggle=\"tab\" href=\"#upcoming\" onclick=sortByTaskTab(\"upcoming\",\""+page+"\");>Upcoming</a></li>"+
										"<li><a data-toggle=\"tab\" href=\"#oldest\" onclick=sortByTaskTab(\"oldest\",\""+page+"\");>Oldest</a></li>"+
										"<li><a data-toggle=\"tab\" href=\"#newest\" onclick=sortByTaskTab(\"newest\",\""+page+"\");>Newest</a></li>"+
										"<li><a data-toggle=\"tab\" href=\"#owner\" onclick=sortByTaskTab(\"owner\",\""+page+"\");>Owner</a></li>"+
										"<li style=\"float:right\"><span id=\"numberoftask\" class=\"numberofanswer\"> "+taskcount+" Tasks</span></li>"+
									"</ul>" + 
									"<div class=\"tab-content\">"+
										"<div id=\"upcoming\" class=\"tab-pane fade in active\">";
				
				t=fob.fetchTask(id,"upcoming", page, who, "sortbytab");
				if(t.equals("Failed")==true) s+="</br><p class=\"t-red>Error while loading tasks try again</p>";
				else if(t.equals("")==true) s+="</br><p class=\"t-blue\">Add new task <i class=\"fa fa-smiley-o\"></i></p>";
				else s+=t;
				
				s+=						"</div>"+
										"<div id=\"oldest\" class=\"tab-pane fade\">"+
										"</div>"+
										"<div id=\"newest\" class=\"tab-pane fade\">"+ 
										"</div>" + 
										"<div id=\"owner\" class=\"tab-pane fade\">"+ 
										"</div>" + 
									"</div>"+
								"</div>"+
							"</div>"+
							"<div class=\"col-md-3\">"+
								"<div class=\"page-header\">"+
									"<span class=\"pageheading\">Recommended Tasks</span>"+
								"</div>"+
								"<div>"+
									"<input type=\"text\" class=\"form-control\" id=\"searchtask\" onkeyup='fetchTasks(\"search\",\""+page+"\");' placeholder=\"type to search tasks\">"+
								"</div><br>"+
								"<div id=\"recommenddiv\" style=\"overflow:auto;\">"+fob.recommendedTasks(id, who, page, "recommended")+"</div>"+
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
