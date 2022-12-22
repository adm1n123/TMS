

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class test
 */
@WebServlet("/Test")
public class Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Test() {
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
		
		String remainder="",t,Head,Navbar="",who,page="userhome",userid,s;
		String id="",Q;
		int count=0;
		boolean valid=true;
		
		Head=fob.head(id);
	//	Navbar=fob.navbarHome(ses);
		s=Head+Navbar;
		
	
			
		s+="<div class=\"container\">\r\n" + 
				"    <div class=\"row\">\r\n" + 
				"        <div class='col-sm-6'>\r\n" + 
				"            <div class=\"form-group\">\r\n" + 
				"                <div class='input-group date' id='datetimepicker1'>\r\n" + 
				"                    <input type='text' class=\"form-control\" />\r\n" + 
				"                    <span class=\"input-group-addon\">\r\n" + 
				"                        <span class=\"glyphicon glyphicon-calendar\"></span>\r\n" + 
				"                    </span>\r\n" + 
				"                </div>\r\n" + 
				"            </div>\r\n" + 
				"        </div>\r\n" + 
				"        <script type=\"text/javascript\">\r\n" + 
				"            $(function () {\r\n" + 
				"                $('#datetimepicker1').datetimepicker();\r\n" + 
				"            });\r\n" + 
				"        </script>\r\n" + 
				"    </div>\r\n" + 
				"</div>";
		s+=		"</body></html>";


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
