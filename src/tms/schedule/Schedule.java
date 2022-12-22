package tms.schedule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Schedule implements ServletContextListener {

	protected boolean sendReminder(String userid, String heading, String date, String taskid) {
		Function fob = new Function();
		String from, password, to="", subject, url, content, s, key = "",notification;
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			rs = smt.executeQuery("select user_email from users where user_id = '"+userid+"'");
			if(rs.next() == true) {
				to = rs.getString("user_email");
			} else {
				return false;
			}

			do{
				key=fob.nextKey();
				rs=smt.executeQuery("select * from remove_reminder where verify_key='"+key+"'");
			}while(rs.next()==true);
			
			from="info-TMS@deepakbaghel.in";
			password="N.6z7kAcAZ@s";
			subject="Reminder - TMS";
			url=fob.domainName()+"/TMS/RemoveReminder?userid="+userid+"&taskid="+taskid+"&email="+to+"&key="+key;
			
			content=
					"<html>"+
						"<body>"+
							"<p>Hello, <b><i>"+userid+"</i></b></p>"+
							"<p>This is to remind about your task <font color=blue><b>"+heading+"</b></font> on <a href=\""+fob.domainName()+"/TMS/Index\"><b>TMS</b></a> scheduled for "+fob.dateRev(date)+" </p>"+
							"<p>To remove task from your todo list <a href=\""+url+"\"><button >click here</button></a></p>"+
							"<p>If unable to click please paste this link into browser's address bar and hit ENTER <a href=\""+url+"\">"+url+"</a></p>"+
							"<p><b>NOTE: This link can be used only once and <font color=red>expires within 24 hours</font>.</b></p>"+
						"</body>"+
					"</html>";
		
			s=fob.sendMail(from, password, to, subject, content);
			if(s.equals("Success")==true){
				smt.executeUpdate("delete from remove_reminder where user_id='"+userid+"'");
				smt.executeUpdate("insert into remove_reminder values ("+taskid+",'"+userid+"','"+to+"','"+key+"',now())");
				smt.executeUpdate("update task_user_map set mail_count = mail_count + 1 where task_id = "+taskid+" and user_id = '"+userid+"'");
				
				/// sending notifications
				HashSet<String> idset=new HashSet<String>();
				notification="You got new <a href=\"Task?id="+taskid+"\">task</a> today <span class=\"t-grey t-sx\"> "+fob.date()+"</span><span>";
				idset.add(userid);
				fob.sendNotification(idset, notification);
				idset.clear();
				return true;
			}
		}catch(Exception e) {}
		return false;
	}
	
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("Scheduler Listener has been shutdown");
	}
	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

		System.out.println("Scheduler Listener initialized.");

		TimerTask object = new MyTimer();
		Timer timer = new Timer();
		
		timer.schedule(object, 1000, 5 * 60 * 1000);
	}
	
	
	class MyTimer extends TimerTask {

		@Override
		public void run() {
			String taskid, heading, date, email, userid;
			int count;
			boolean imp;
			Schedule ob = new Schedule();
			
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement(),smt2 = cn.createStatement();
				ResultSet rs, rs2;
				// only imp 
				rs = smt.executeQuery("SELECT * FROM task WHERE type='important' and (date BETWEEN DATE(CONVERT_TZ(DATE_SUB(NOW(), INTERVAL 1 WEEK), '+00:00', '+10:30')) AND DATE(CONVERT_TZ(DATE_SUB(NOW(), INTERVAL 6 DAY), '+00:00', '+10:30')))");
				while(rs.next() == true) {
					taskid = rs.getString("id");
					heading = rs.getString("heading");
					date = rs.getString("date");
					rs2 = smt2.executeQuery("select * from task_user_map where task_id = "+taskid);
					while(rs2.next() == true) {
						count = rs2.getInt("mail_count");
						userid = rs2.getString("user_id");
						if(count == 0) {
							ob.sendReminder(userid, heading, date, taskid);
						}
					}
				}
				/// regular or imp today
				rs = smt.executeQuery("SELECT * FROM task WHERE date = DATE(CONVERT_TZ(NOW(), '+00:00', '+10:30'))");
//				rs = smt.executeQuery("SELECT * FROM task WHERE date = CURDATE()");
				while(rs.next() == true) {
					taskid = rs.getString("id");
					heading = rs.getString("heading");
					date = rs.getString("date");
					
					if(rs.getString("type").equals("important") == true) {
						imp = true;
					} else {
						imp = false;
					}
					rs2 = smt2.executeQuery("select * from task_user_map where task_id = "+taskid);
					while(rs2.next() == true) {
						count = rs2.getInt("mail_count");
						userid = rs2.getString("user_id");
						if(count == 0 || count == 1 && imp == true) {
							ob.sendReminder(userid, heading, date, taskid);
						}
					}
				}
			}catch(Exception e) {

			}
		}
	}
}