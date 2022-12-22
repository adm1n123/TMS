

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class FetchTasks
 */
@WebServlet("/FetchTasks")
public class FetchTasks extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FetchTasks() {
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

		Functions fob = new Functions();
		PrintWriter pr = response.getWriter();
		HttpSession ses=request.getSession(false);
		if(fob.validate(ses)==false) {return;}
		String id="",who="",which,page,regex;
		try{
			try{
				id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				id=ses.getAttribute("admin_id").toString();who="admin";
			}
		}catch(Exception e){ response.sendRedirect("Login");return;}

		which = request.getParameter("which");
		page = request.getParameter("page");
		regex = request.getParameter("regex");
		
		if(fob.equals(which, "followed") == true) {
			pr.println("Success "+fob.tasksFollowed(id,who,page, which));
		} else if(fob.equals(which, "recommended") == true) {
			pr.println("Success "+fob.recommendedTasks(id, who, page, which));
		} else if(fob.equals(which, "search") == true) {
			pr.println("Success "+fob.searchTasks(id, regex, who, page, which));
		}
	}

}
