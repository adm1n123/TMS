
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Date;
import java.util.regex.Matcher;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Functions
 */
@WebServlet("/Functions")
class Functions extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    protected Functions() {
        super();
        // TODO Auto-generated constructor stub
    }
    /////////////// generate key ////////////////////////////
    private SecureRandom random = new SecureRandom();
    protected String nextKey() {
    	String s;
    	while((s=new BigInteger(250, random).toString(32)).length()!=50)
    		s=new BigInteger(250, random).toString(32);
      return s;
    }

    ////////// gravatar/////// //////////////////////////////////////
	private Pattern pat;
	private Matcher mat;
	private String reg;
	
	protected String hex(byte[] array) {
	      StringBuffer sb = new StringBuffer();
	      for (int i = 0; i < array.length; ++i) {
	      sb.append(Integer.toHexString((array[i]
	          & 0xFF) | 0x100).substring(1,3));        
	      }
	      return sb.toString();
	  }
	
	protected String md5Hex (String message) {
	      try {
	    	  MessageDigest md = MessageDigest.getInstance("MD5");
	    	  return hex (md.digest(message.getBytes("CP1252")));
	      } catch (NoSuchAlgorithmException e) {
	      } catch (UnsupportedEncodingException e) {
	      }
	      return null;
	  }
	protected String dateRev(String dt) {
		String date[];
		date=dt.split("-");
		String month[]={"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		return date[2]+"-"+month[Integer.parseInt(date[1])]+"-"+date[0];
	}
	
	protected String date(String datetime){
		String s="";
		if(datetime.equalsIgnoreCase("now()") == true);
		else if(isAlphabet(datetime) == 1) return s;
		else datetime = "'"+datetime+"'";
		try{
		   	try{
	    		Class.forName("com.mysql.jdbc.Driver").newInstance();
	    		Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
	    		Statement smt=cn.createStatement();
	    		ResultSet rs = smt.executeQuery("select convert_tz("+datetime+",'+00:00','+10:30') as datetime"); // chicago to india
	    		if(rs.next() == true) {
	    			datetime = rs.getString("datetime");
	    		}
	    		rs.close();smt.close();cn.close();
	    	}catch(Exception e){
	    		
	    	}
			String date[],time[],sb[]=datetime.split(" ");
			date=sb[0].split("-");
			time=sb[1].split(":");
			String month[]={"","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			s= date[2]+"-"+month[Integer.parseInt(date[1])]+"-"+date[0]+" at "+time[0]+":"+time[1];
		}catch(Exception e){}
		return s;
	}
	
	protected String date(){
	/*	String s="",st[],date,time[];
		st=(new java.util.Date()).toString().split(" ");
		time=st[3].split(":");
		date=st[2]+" "+st[1]+" "+st[5];
		s=date+" at "+time[0]+":"+time[1];
		return s;
	*/
		return date("now()");
	}
	
	///////////////////////////////////////////////////////// Login ////////////////////////////////////////////////////////////////////////
	
	protected String login(String id, String password, HttpServletRequest request){
		Functions fob=new Functions();
		HttpSession ses=request.getSession(false);
		String Q,s="Failed",fname,lname;
		int c=0;
		boolean valid=true;
		String key;
		
		if(fob.validId(id)==false) valid=false;
		if(fob.validPassword(password)==false) valid=false;
		if(valid==false){ return s;}
		
		try{
			ses.invalidate();
		}catch(Exception e){}
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			cn.setAutoCommit(false);
			do{
				key=fob.nextKey();
				rs=smt.executeQuery("select * from active_users where session_key='"+key+"' limit 1");
			}while(rs.next()==true);
			Q="select * from users where user_id='"+id+"'";
			rs=smt.executeQuery(Q);
			while(rs.next()) c++;
			rs.first();
			if(c==1&&rs.getString("user_id").equals(id)==true&&rs.getString("user_pass").equals(password)==true){
				fname=rs.getString("user_fname");lname=rs.getString("user_lname");
				ses=request.getSession(true);
				ses.setAttribute("user_id",id);
				ses.setAttribute("user_fname", fname);
				ses.setAttribute("user_lname", lname);
				ses.setAttribute("Time", fob.date(rs.getString("last_login")));
				ses.setAttribute("emailhash",fob.md5Hex(rs.getString("user_email")));
				ses.setAttribute("key", key);
				smt.executeUpdate("delete from active_users where user_id='"+id+"'");
				smt.executeUpdate("insert into active_users values (null,'"+id+"','"+fname+"','"+lname+"','"+key+"',now())");
				smt.executeUpdate("update users set last_login=now() where user_id='"+id+"'");
				cn.commit();
				s="User";
				clearSession();
				
			} else if(c==0){
				do{
					key=fob.nextKey();
					rs=smt.executeQuery("select * from active_admin where session_key='"+key+"'");
				}while(rs.next()==true);
				Q="select * from admin where admin_id='"+id+"'";
				rs=smt.executeQuery(Q);
				while(rs.next()) c++;
				rs.first();
				if(c==1&&rs.getString("admin_id").equals(id)==true&&rs.getString("admin_pass").equals(password)==true){
					ses=request.getSession(true);
					ses.setAttribute("admin_id",id);
					ses.setAttribute("admin_fname", rs.getString("admin_fname"));
					ses.setAttribute("admin_lname", rs.getString("admin_lname"));
					ses.setAttribute("Time", fob.date(rs.getString("last_login")));
					ses.setAttribute("key", key);
					smt.executeUpdate("delete from active_admin where admin_id='"+id+"'");
					smt.executeUpdate("insert into active_admin values (null,'"+id+"','"+key+"',now())");
					smt.executeUpdate("update admin set last_login=now() where admin_id='"+id+"'");
					cn.commit();
					s="Admin";
				}
				else{
					s="Wrong";
				}
			}
			cn.rollback();rs.close();smt.close();cn.close();
		}catch(Exception e){
			
		}
		
		return s;
	}
	////////////////////////////////////////////// validate session ///////////////////////////////////////////////////////////////////////////////////
    protected boolean validate(HttpSession ses){
    	String id="",key="",Q,fname="",lname="",who="";
    	Functions fob=new Functions();
    	boolean c=false,valid=true;
    	int count=0;
    	
    	try{
			try{
				fname=ses.getAttribute("user_fname").toString();lname=ses.getAttribute("user_lname").toString();id=ses.getAttribute("user_id").toString();who="user";
			}catch(Exception p){
				fname=ses.getAttribute("admin_fname").toString();lname=ses.getAttribute("admin_lname").toString();id=ses.getAttribute("admin_id").toString();who="admin";
			}
			ses.getAttribute("Time").toString();
			key=ses.getAttribute("key").toString();
			if(fob.validName(fname)==false) valid=false;
			if(fob.validName(lname)==false) valid=false;
			if(fob.validId(id)==false) valid=false;
			if(fob.validKey(key)==false) valid=false;
			if(valid==false) return false;
		}catch(Exception e){ return false;}
    	
    	try{
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q="select * from active_users where user_id='"+id+"'";
			rs=smt.executeQuery(Q);
			while(rs.next()==true) count++;
			if(count==1){
				rs.first();
				if(rs.getString("session_key").equals(key)==true){
					smt.executeUpdate("update active_users set datetime = now() where user_id = '"+id+"'");
					clearSession();
					c=true;
				}
			} else if(count==0&&who.equals("admin")==true){
				Q="select * from active_admin where admin_id='"+id+"'";
				rs=smt.executeQuery(Q);
				while(rs.next()==true) count++;
				if(count==1){
					rs.first();
					if(rs.getString("session_key").equals(key)==true) c=true;
				}
			}
			rs.close();smt.close();cn.close();
    	}catch(Exception e){
    	}
    	   	return c;
    }
    ////////////////////////////////////////////// Clear session ///////////////////////////////////////////////////////////////////////////////////
    protected void clearSession(){
    	try{
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
    		Statement smt=cn.createStatement();
    		smt.executeUpdate("delete from active_users where datetime < (now() - interval 20 minute)");
    		smt.close();cn.close();
    	}catch(Exception e){
    	}
    }

	//////////////////////////////////////////////////////////// Send mail /////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String sendMail(final String from,final String password,String to,String subject, String content){
		Functions fob=new Functions();
		boolean valid=true;
		String s="Failed";
		int c=2;
		
		if(fob.validEmail(from)==false) valid=false;
		if(fob.validPassword(password)==false) valid=false;
		if(fob.validEmail(to)==false) valid=false;
		if(fob.validText(subject)==false) valid=false;
		if(fob.validText(content)==false) valid=false;
		if(valid==false) return s;
		
		do{
			s="Failed";
			//Get the session object  
			Properties props = new Properties();
			props.put("mail.smtp.host", "xjdz4.dailyrazor.com");
			props.put("mail.smtp.socketFactory.port", "465");  
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");  
			props.put("mail.smtp.auth", "true");  
			props.put("mail.smtp.port", "465");  
			 
			Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {  
				protected PasswordAuthentication getPasswordAuthentication() {  
					return new PasswordAuthentication(from,password);
				}  
			});  
			//compose message  
			try{
				MimeMessage message = new MimeMessage(session);  
				message.setFrom(new InternetAddress(from));  
				message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
				message.setSubject(subject);
				message.setContent(content,"text/html");  
				Transport.send(message);
				s="Success";
			} catch (Exception e) {
			}
			if(s.equals("Failed")==true) c--;
			else c=0;
		}while(c>0);
		 
		return s;
	}
		
	////////////////////////////////////////////////////////// no cache ///////////////////////////////////////////////////////////////////////////////////////////////
	
	protected void noCache(HttpServletResponse response){
		response.setHeader("Cache-Control","no-cache"); //Forces caches to obtain a new copy of the page from the origin server
		response.setHeader("Cache-Control","no-store"); //Directs caches not to store the page under any circumstance
		response.setDateHeader("Expires", 0); //Causes the proxy cache to see the page as "stale"
		response.setHeader("Pragma","no-cache"); //HTTP 1.0 backward compatibility
	}
	
	///////////////////////////////////////// get question /// pages: Question//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String question(String questionid,String id,String page){
		Functions fob=new Functions();
		String s="",Q;
		boolean valid=true;
		
		if(fob.validNumber(questionid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			Q="select * from questions where id="+questionid+" limit 1";
			rs=smt.executeQuery(Q);
			
			if(rs.next()==true){
				s=fob.question(id, page, rs);
			} else{
				s="NotExist";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		return s;
	}
	
	protected String question(String id,String page,ResultSet prs){
		String s="",who="",date,questionid,deletequestion,Q,upvote,downvote,readinglist,reportbutton,totalupvote,totaldownvote,question,author,upvotesign,downvotesign,editbutton,commentcount,answercount,tag,tags[],tagdiv,inputtag,emailhash;
		Functions fob=new Functions();
		int count,tl;
		boolean valid=true;
		
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			rs=smt.executeQuery("select * from admin where admin_id='"+id+"' limit 1");
			if(rs.next()==true) who="admin";else who="user";
			
			// include from prs from here
			upvote="<i class=\"fa fa-thumbs-o-up fa-lg\"></i> upvote";downvote="<i class=\"fa fa-thumbs-o-down fa-lg\"></i>";readinglist="<i class=\"glyphicon glyphicon-plus\"></i>";
			upvotesign="+";downvotesign="-";deletequestion="";editbutton="";answercount="";tagdiv="";inputtag="";emailhash="";
			questionid=prs.getString("id");
			totalupvote=prs.getString("upvote");if(totalupvote.equals("0")) upvotesign="";
			totaldownvote=prs.getString("downvote");if(totaldownvote.equals("0")) downvotesign="";
			commentcount=prs.getString("commentcount");
			question=prs.getString("question");
			author=prs.getString("author");
			date=fob.date(prs.getString("datetime"));
			
			if(author.equals(id)||who.equals("admin")){
				deletequestion="<button class=\"btnCustom btn-Custom-default b-s-delete\" id=\"deletequestion"+questionid+"\" onclick=deleteMyQuestion(\""+questionid+"\",\""+page+"\");><i class=\"glyphicon glyphicon-trash i-red\"></i></button>";
				editbutton="<button class=\"btnCustom btn-Custom-default b-s-edit\" id=\"editquestionbutton"+questionid+"\" onclick=edit(\""+questionid+"\",\""+id+"\",\"question\",\"button\");><i class=\"glyphicon glyphicon-edit i-blue\"></i></button> ";
			}
			// stop from prs
			if(page.equals("community")==true||page.equals("adminhome")==true||page.equals("searchoraskquestion")==true){
				Q="select author from answers where question_id="+questionid+" order by datetime desc";
				rs=smt.executeQuery(Q);
				HashSet<String> idset=new HashSet<String>();
				String uid;
				int c=5;
				count=0;while(rs.next()==true){
					idset.add(rs.getString("author"));
					count++;
				}
				if(count>0){
					answercount="</br><div class=\"answercountdiv\"><span class=\"t-grey t-md f-right\"> <span id=\"answercount"+questionid+"\">"+count+"</span> Answers &nbsp;</span>&nbsp; <span class=\"t-sm\">answered by ";
					Iterator<String> iterator=idset.iterator();
					while(iterator.hasNext()==true&&c>=0){
						uid=iterator.next();c--;
						answercount+="<a href=\"Profile?id="+uid+"\">@"+uid+"</a> ";
					}
					answercount+=" +more.</span></div>";
				} else {
					answercount="</br><div class=\"answercountdiv\"><span class=\"t-grey t-md f-right\"><span id=\"answercount"+questionid+"\">"+count+"</span> Answers &nbsp;</span>&nbsp; <span class=\"t-sm\">Be first to answer </span></div>";
				}
				
			}
			//////////////   fetching tags ///////////////////////
			StringBuilder sb1=new StringBuilder(""),sb2=new StringBuilder("");
			rs=smt.executeQuery("select tag from tags where id in (select tag_id from tag_map where question_id="+questionid+")");
			while(rs.next()==true){
				tag=rs.getString("tag");
				sb1.append("<span class=\"taglabel t-md\"><a href=\"Tag?name="+tag+"\">"+tag+"</a></span> ");
				sb2.append(tag+" ");
			}
			tagdiv=sb1.toString();inputtag=sb2.toString();
			if(inputtag.length()>0) inputtag=inputtag.substring(0, inputtag.length()-1);
			tagdiv="<div class=\"tagdiv\" id=\"tagdiv"+questionid+"\">"+tagdiv+"</div>";
			inputtag="<input style=\"display:none\" type=\"text\" id=\"inputtag"+questionid+"\" class=\"form-control input-sm\" value=\""+inputtag+"\" placeholder=\"tags separated by space\">";
			////\\\\\\\\\////////\\\\\\\\\\\\\\\\\\\///////\\\\\\\\\\\\\\\\\\//////\\\\\\\
			////////// gravatar /////////////
			rs=smt.executeQuery("select user_email from users where user_id='"+author+"'");
			if(rs.next()==true) emailhash=fob.md5Hex(rs.getString("user_email")); else emailhash="82c79e1d6a75e9fb03edc6b53f859a25";
			/////////\\\\\\\\\\
			Q="select * from question_upvote where (id="+questionid+" and user_id='"+id+"') limit 1";
			rs=smt.executeQuery(Q);
			if(rs.next()) upvote="<i class=\"fa fa-thumbs-up fa-lg i-link\"></i><span class=\"t-link\"> upvote</span>";
			
			Q="select * from question_downvote where (id="+questionid+" and user_id='"+id+"') limit 1";
			rs=smt.executeQuery(Q);
			if(rs.next()) downvote="<i class=\"fa fa-thumbs-down fa-lg i-link\"></i>";
			
			Q="select * from readinglist where (question_id="+questionid+" and user_id='"+id+"') limit 1";
			rs=smt.executeQuery(Q);
			if(rs.next()) readinglist="<i class=\"glyphicon glyphicon-ok\"></i>";
			
			Q="select * from suspicious where id="+questionid+" and type=\'question\' and reporter='"+id+"' limit 1";
			rs=smt.executeQuery(Q);
			reportbutton="<button class=\"btnCustom btn-Custom-default b-s-report\" id=\"reportquestion"+questionid+"\" onclick=reportSuspicious(\"question\",\""+questionid+"\",\""+id+"\"); data-toggle=\"tooltip\" title=\"Report\" ><i class=\"glyphicon glyphicon-alert i-orange\"></i></button> ";
			if(rs.next()) reportbutton="<button class=\"btnCustom btn-Custom-default b-s-report\" id=\"reportquestion"+questionid+"\" disabled onclick=reportSuspicious(\"question\",\""+questionid+"\",\""+id+"\"); data-toggle=\"tooltip\" title=\"Report\"><i class=\"glyphicon glyphicon-alert i-orange\"></i></button> ";
			
			s+=
						"<div style=\"border-bottom:10px solid transparent;\">"+tagdiv+inputtag+"</div>"+
						"<div>"+
							"<span class=\"t-md\"> <img class=\"img-circle\" alt=\"\" src=\"https://www.gravatar.com/avatar/"+emailhash+"?s=30.jpg\" /> <a href=\"Profile?id="+author+"\">"+author+"</a></span> <span class=\"t-xs t-grey\"> "+date+"</span>"+
							"<div class=\"btn-group f-right\">"+
								"<button class=\"btnCustom btn-Custom-default b-s-readinglist\" id=\"addreadinglist"+questionid+"\" onclick=addReadingList(\""+questionid+"\",\""+id+"\"); data-toggle=\"tooltip\" title=\"Reading list\">"+readinglist+"</button> "+
								reportbutton+
								editbutton+
								deletequestion+
							"</div>"+
						"</div>"+
						"<div class=\"clearfix\"></div></br>"+
						"<div class=\"questiontextdiv\"><a href=\"Question?id="+questionid+"\" class=\"questiontextlink\"><span id=\"questiontext"+questionid+"\">"+question+"</span></a></div>"+
						"<div>"+
							"<div>"+
								"<div class=\"btn-group\">"+
									"<button class=\"btnCustom btn-Custom-default b-upvote\" id=\"upvotequestion"+questionid+"\" onclick=vote(\"question\",\"upvote\",\""+questionid+"\",\""+id+"\");>"+upvote+" | <span id=\"totalupvotequestion"+questionid+"\"><span class=\"t-link\">"+upvotesign+totalupvote+"</span></span></button> "+
									"<button class=\"btnCustom btn-Custom-default b-downvote\" id=\"downvotequestion"+questionid+"\" onclick=vote(\"question\",\"downvote\",\""+questionid+"\",\""+id+"\");>"+downvote+" | <span id=\"totaldownvotequestion"+questionid+"\"><span class=\"i-red\">"+downvotesign+totaldownvote+"</span></span></button> "+
								"</div>"+
								"<button class=\"btn btn-link btn-sm b-comment\" id=\"commentquestionbutton"+questionid+"\" onclick=commentButton(\"question\",\""+questionid+"\",\""+id+"\");><i class=\"fa fa-comments-o fa-lg\"></i> comments</button><span class=\"commentlabel\" id=\"commentcountquestion"+questionid+"\">"+commentcount+"</span> "+
								"<button class=\"btn btn-link btn-sm b-answer\" id=\"writeanswerbutton"+questionid+"\" onclick=answerButton(\""+questionid+"\");><i class=\"fa fa-pencil-square-o fa-lg\"></i> answer</button> "+
								answercount+
							"</div>"+
							"<div id=\"commentquestiondiv"+questionid+"\" class=\"display-n\"></br>"+
								"<div id=\"commenttextquestiondiv"+questionid+"\"></div>"+
								"<div id=\"commentquestionpreview"+questionid+"\" class=\"t-preview\"></div>"+
								"<span id=\"commentquestionspan"+questionid+"\"></span></br>"+
								"<textarea class=\"form-control\" id=\"commentquestiontextarea"+questionid+"\" type=\"text\" rows=\"3\" maxlength=\"200\" onkeyup=commentType(\"question\",\""+questionid+"\");></textarea>"+
								"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=commentSave(\"question\",\""+questionid+"\",\""+id+"\");><i class=\"fa fa-comment-o fa-lg\"></i> Add</button></div>"+
							"</div>"+
							"<div class=\"clearfix\"></div>"+
							"<div id=\"editquestiondiv"+questionid+"\" class=\"display-n\">"+
								"<span id=\"editquestionspan"+questionid+"\"></span></br>"+
								"<textarea class=\"form-control\" id=\"editquestiontextarea"+questionid+"\" type=\"text\" rows=\"3\" maxlength=\"200\" onkeyup=edit(\""+questionid+"\",\""+id+"\",\"question\",\"textarea\");></textarea>"+
								"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=edit(\""+questionid+"\",\""+id+"\",\"question\",\"save\");><i class=\"glyphicon glyphicon-save\"></i> Save</button></div>"+
							"</div>"+
							"<div class=\"clearfix\"></div>"+
							"<div id=\"writeanswerdiv"+questionid+"\" class=\"display-n\"></br>"+
								"<div  class=\"t-preview\" id=\"writeanswerpreview"+questionid+"\"></div>"+
								"<span id=\"writeanswerspan"+questionid+"\"></span></br>"+
								"<textarea class=\"form-control\" id=\"writeanswertextarea"+questionid+"\" type=\"text\" rows=\"15\" onKeyUp=writeAnswer(\""+questionid+"\"); placeholder=\"write your answer here\"></textarea>"+
								"<div class=\"b-save\"><button class=\"btn btn-success\" id=\"sendbutton"+questionid+"\" disabled onclick=sendAnswer(\""+questionid+"\",\""+id+"\",\""+page+"\");><i class=\"glyphicon glyphicon-ok\"></i> Done</button></div>"+
							"</div>"+
						"</div>";
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s=e+"Failed";
		}
		return s;
	}
	
	///////////////////////////////////////////////////////////// get answer /// pages: Question, ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String answer(String answerid,String id,String page){
		Functions fob=new Functions();
		String s="",Q;
		boolean valid=true;
		
		if(fob.validNumber(answerid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			Q="select * from answers where id="+answerid+" limit 1";
			rs=smt.executeQuery(Q);
			if(rs.next()){
				s=fob.answer(rs.getString("question_id"), id, page, rs);
			} else{
				s="NotExist";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		return s;
		
	}
	
	protected String answer(String questionid,String id,String page,ResultSet prs){
		Functions fob=new Functions();
		String s="",who="",date,answerid,deleteanswer,Q,answer,upvote,downvote,reportbutton,totalupvote,totaldownvote,author,upvotesign,downvotesign,editbutton,commentcount,questionlink,emailhash;
		boolean valid=true;
		if(fob.validNumber(questionid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			rs=smt.executeQuery("select * from admin where admin_id='"+id+"' limit 1");
			if(rs.next()==true) who="admin";else who="user";
			
			// include from prs
			upvote="<i class=\"fa fa-thumbs-o-up fa-lg\"></i> upvote";downvote="<i class=\"fa fa-thumbs-o-down fa-lg\"></i>";upvotesign="+";downvotesign="-";deleteanswer="";editbutton="";emailhash="";
			answerid=prs.getString("id");
			totalupvote=prs.getString("upvote");if(totalupvote.equals("0")) upvotesign="";
			totaldownvote=prs.getString("downvote");if(totaldownvote.equals("0")) downvotesign="";
			commentcount=prs.getString("commentcount");
			date=fob.date(prs.getString("datetime"));
			answer=prs.getString("answer");
			author=prs.getString("author");
			if(page.equals("question")==true) questionlink="";
			else questionlink="<span class=\"f-right\"><a href=\"Question?id="+questionid+"\"><span class=\"t-md\" style=\"font-weight: bold\">Question</span></a></span>";
			if(author.equals(id)||who.equals("admin")){
				deleteanswer="<button class=\"btnCustom btn-Custom-default b-s-delete\" id=\"deleteanswer"+answerid+"\" onclick=deleteMyAnswer(\""+answerid+"\",\""+questionid+"\",\""+page+"\");><i class=\"glyphicon glyphicon-trash i-red\"></i></button>";
				editbutton="<button class=\"btnCustom btn-Custom-default b-s-edit\" id=\"editanswerbutton"+answerid+"\" onclick=edit(\""+answerid+"\",\""+id+"\",\"answer\",\"button\");><i class=\"glyphicon glyphicon-edit i-blue\"></i></button> ";
			}
			////////// gravatar /////////////
			rs=smt.executeQuery("select user_email from users where user_id='"+author+"'");
			if(rs.next()==true) emailhash=fob.md5Hex(rs.getString("user_email")); else emailhash="82c79e1d6a75e9fb03edc6b53f859a25";
			///// stop from prs
			Q="select * from answer_upvote where (id="+answerid+" and user_id='"+id+"') limit 1";
			rs=smt.executeQuery(Q);
			if(rs.next()) upvote="<i class=\"fa fa-thumbs-up fa-lg i-link\"></i><span class=\"t-link\"> upvote</span>";
			rs=smt.executeQuery(Q);
			
			Q="select * from answer_downvote where (id="+answerid+" and user_id='"+id+"') limit 1";
			rs=smt.executeQuery(Q);
			if(rs.next()) downvote="<i class=\"fa fa-thumbs-down fa-lg i-link\"></i>";
			rs=smt.executeQuery(Q);
			
			Q="select * from suspicious where id="+answerid+" and type=\'answer\' and reporter='"+id+"' limit 1";
			rs=smt.executeQuery(Q);
			reportbutton="<button class=\"btnCustom btn-Custom-default b-s-report\" id=\"reportanswer"+answerid+"\" onclick=reportSuspicious(\"answer\",\""+answerid+"\",\""+id+"\"); data-toggle=\"tooltip\" title=\"Report\"><i class=\"glyphicon glyphicon-alert\"></i></button> ";
			if(rs.next()) reportbutton="<button class=\"btnCustom btn-Custom-default b-s-report\" id=\"reportanswer"+answerid+"\" disabled onclick=reportSuspicious(\"answer\",\""+answerid+"\",\""+id+"\"); data-toggle=\"tooltip\" title=\"Report\"><i class=\"glyphicon glyphicon-alert\"></i></button> ";
			
			s+=
						"<div>"+
							"<span class=\"t-md\"> <img class=\"img-circle\" alt=\"\" src=\"https://www.gravatar.com/avatar/"+emailhash+"?s=30.jpg\" /> <a href=\"Profile?id="+author+"\">"+author+"</a></span> <span class=\"t-xs t-grey\"> "+date+"</span>"+
							"<div class=\"btn-group f-right\">"+
								reportbutton+
								editbutton+
								deleteanswer+
							"</div>"+
						"</div>"+
						"<div class=\"clearfix\"></div></br>"+
						"<div class=\"answertextdiv\"><span id=\"answertext"+answerid+"\">"+answer+"</span></div>"+
						"<div>"+
							"<div>"+
								"<button class=\"btnCustom btnCustom-default b-upvote\" id=\"upvoteanswer"+answerid+"\" onclick=vote(\"answer\",\"upvote\",\""+answerid+"\",\""+id+"\");>"+upvote+" | <span id=\"totalupvoteanswer"+answerid+"\"><span class=\"t-link\">"+upvotesign+totalupvote+"</span></span></button> "+
								"<button class=\"btnCustom btnCustom-default b-downvote\" id=\"downvoteanswer"+answerid+"\" onclick=vote(\"answer\",\"downvote\",\""+answerid+"\",\""+id+"\");>"+downvote+" | <span id=\"totaldownvoteanswer"+answerid+"\"><span class=\"t-red\">"+downvotesign+totaldownvote+"</span></span></button> "+
								"<button class=\"btn btn-link btn-sm b-comment\" id=\"commentanswerbutton"+answerid+"\" onclick=commentButton(\"answer\",\""+answerid+"\",\""+id+"\");><i class=\"fa fa-comments-o fa-lg\"></i> comments</button><span class=\"commentlabel\" id=\"commentcountanswer"+answerid+"\">"+commentcount+"</span>"+
								questionlink+
							"</div>"+
							"<div id=\"commentanswerdiv"+answerid+"\" class=\"display-n\"></br>"+
								"<div id=\"commenttextanswerdiv"+answerid+"\"></div>"+
								"<div id=\"commentanswerpreview"+answerid+"\" class=\"t-preview\"></div>"+
								"<span id=\"commentanswerspan"+answerid+"\"></span></br>"+
								"<textarea class=\"form-control\" id=\"commentanswertextarea"+answerid+"\" type=\"text\" rows=\"3\" maxlength=\"200\" onkeyup=commentType(\"answer\",\""+answerid+"\");></textarea>"+
								"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=commentSave(\"answer\",\""+answerid+"\",\""+id+"\");><i class=\"fa fa-comment-o fa-lg\"></i> Add</button></div>"+
							"</div>"+
							"<div class=\"clearfix\"></div>"+
							"<div id=\"editanswerdiv"+answerid+"\" class=\"display-n\">"+
								"<span id=\"editanswerspan"+answerid+"\"></span></br>"+
								"<textarea class=\"form-control\" id=\"editanswertextarea"+answerid+"\" type=\"text\" rows=\"15\" maxlength=\"2000\" onkeyup=edit(\""+answerid+"\",\""+id+"\",\"answer\",\"textarea\");></textarea>"+
								"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=edit(\""+answerid+"\",\""+id+"\",\"answer\",\"save\");><i class=\"glyphicon glyphicon-save\"></i> Save</button></div>"+
							"</div>"+
						"</div>";
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		return s;
		
	}
	
	/////////////////////////////////////////////////////////////// sort answer by popularity /// pages: Question /////////////////////////////////////////////////////////////////////
	
	protected String popularAnswers(String questionid, String id, String page){
		Functions fob=new Functions();
		String s="",Q,t;
		boolean valid=true;
		if(fob.validNumber(questionid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q="select *, ((cast(upvote as signed)+1)/(cast(downvote as signed)+1)+(cast(upvote as signed)-cast(downvote as signed))) as points from answers where question_id="+questionid+" order by points desc";
			rs=smt.executeQuery(Q);
			
			while(rs.next()==true){
				t=fob.answer(questionid, id, page, rs);
				if(t.equals("Failed")==true||t.length()==0) continue;
				s+=	"<table class=\"table table-hover\">"+
						"<tr>"+
							"<td>"+
								t+
							"</td>"+
						"</tr>"+
					"</table>";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	
	/////////////////////////////////////////////////////////////// sort answer by oldest /// pages: Question /////////////////////////////////////////////////////////////////////
	
	protected String oldAnswers(String questionid, String id, String page){
		Functions fob=new Functions();
		int n;
		String s="",t,Q;
		boolean valid=true;
		if(fob.validNumber(questionid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q="select * from answers where question_id="+questionid+" order by datetime asc";
			rs=smt.executeQuery(Q);
			n=0;
			while(rs.next()==true&&n<100){
				t=fob.answer(questionid, id, page,rs);
				if(t.equals("Failed")==true||t.length()==0) continue;
				s+=	"<table class=\"table table-hover\">"+
						"<tr>"+
							"<td>"+
								t+
							"</td>"+
						"</tr>"+
					"</table>";
				n++;
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	
	/////////////////////////////////////////////////////////////// sort answer by new /// pages: Question /////////////////////////////////////////////////////////////////////
	
	protected String newAnswers(String questionid, String id, String page){
		Functions fob=new Functions();
		int n;
		String s="",t,Q;
		boolean valid=true;
		if(fob.validNumber(questionid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q="select * from answers where question_id="+questionid+" order by datetime desc";
			rs=smt.executeQuery(Q);
			n=0;
			while(rs.next()==true&&n<100){
				t=fob.answer(questionid, id, page, rs);
				if(t.equals("Failed")==true||t.length()==0) continue;
				s+=	"<table class=\"table table-hover\">"+
						"<tr>"+
							"<td>"+
								t+
							"</td>"+
						"</tr>"+
					"</table>";
				n++;
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	/////////////////////////////////////////////////////////////// my answers /// pages: Question /////////////////////////////////////////////////////////////////////
	
	protected String myAnswers(String questionid, String id, String page){
		Functions fob=new Functions();
		String s="",t,Q;
		boolean valid=true;
		if(fob.validNumber(questionid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q="select * from answers where question_id="+questionid+" and author='"+id+"' order by datetime desc";
			rs=smt.executeQuery(Q);
			while(rs.next()==true){
				t=fob.answer(questionid, id, page, rs);
				if(t.equals("Failed")==true||t.length()==0) continue;
				s+=	"<table class=\"table table-hover\">"+
						"<tr>"+
							"<td>"+
								t+
							"</td>"+
						"</tr>"+
					"</table>";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	
	////////////////////////////////////////////////////////////////////////// get comment /// pages: Question, //////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String comments(String type,String typeid,String id){
		String s="",who="",author,date,comment,deletecomment,editbutton,commentid;
		Functions fob=new Functions();
		boolean valid=true;
		if(fob.validNumber(typeid)==false) valid=false;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(type)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			rs=smt.executeQuery("select * from admin where admin_id='"+id+"' limit 1");
			if(rs.next()==true) who="admin";else who="user";
			
			rs=smt.executeQuery("select * from comments where (type='"+type+"' and typeid="+typeid+") order by datetime asc");
			while(rs.next()==true){
				deletecomment="";editbutton="";
				commentid=rs.getString("id");
				author=rs.getString("author");
				comment=rs.getString("comment");
				date=fob.date(rs.getString("datetime"));
				if(author.equals(id)||who.equals("admin")){
					deletecomment="<button class=\"btnCustom b-s-delete\" onclick=commentDelete(\""+type+"\",\""+typeid+"\",\""+id+"\",\""+commentid+"\");><i class=\"glyphicon glyphicon-remove\"></i></button>";
					editbutton="<button class=\"btnCustom btn-Custom-default btn-xs b-s-edit\" id=\"editcommentbutton"+commentid+"\" onclick=edit(\""+commentid+"\",\""+id+"\",\"comment\",\"button\");><i class=\"glyphicon glyphicon-edit i-blue\"></i></button> ";
				}
				s+=
						"<tr id=\"commentrow"+commentid+"\">"+ // id to delete comment
							"<td>"+
								"<div class=\"btn-group f-right\">"+
									editbutton+
									deletecomment+
								"</div>"+
								"<span id=\"commenttext"+commentid+"\" class=\"t-grey\">"+comment+"</span> "+ // to break the word and prevent go beyond the column width
								"<span class=\"nowrap\"> - <a href=\"Profile?id="+author+"\">@"+author+"</a> <span class=\"t-grey t-xs\"> on "+date+"</span></span>"+
								"<div class=\"clearfix\"></div>"+
								"<div id=\"editcommentdiv"+commentid+"\" class=\"display-n\">"+
									"<span id=\"editcommentspan"+commentid+"\"></span></br>"+
									"<textarea class=\"form-control\" id=\"editcommenttextarea"+commentid+"\" type=\"text\" rows=\"3\" maxlength=\"200\" onkeyup=edit(\""+commentid+"\",\""+id+"\",\"comment\",\"textarea\");></textarea>"+
									"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=edit(\""+commentid+"\",\""+id+"\",\"comment\",\"save\");><i class=\"glyphicon glyphicon-save\"></i> Save</button></div>"+
								"</div>"+
							"</td>"+
						"</tr>";
			}
			
			s=
				"<table class=\"table table-bordered\" id=\"commenttable"+type+typeid+"\">"+
					s+
				"</table>";
			rs.close();smt.close();cn.close();
		}catch(Exception e){
			s="Failed";
		}
		return s;
	}
	

	
	//////////////////////////////////////////////////////////// Inbox unread messages // pages inbox /////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String inboxUnreadMessages(String id,ResultSet prs){
		
		Functions fob=new Functions();
		String s="",messageid,sender,date;
		boolean valid=true;
		if(fob.validId(id)==false) valid=false;
		if(valid==false) return s;
		
		try{
			prs.beforeFirst();
			s+=		"<table id=\"unreadtable\" class=\"table table-striped table-hover\">";
			while(prs.next()==true){
				messageid=prs.getString("id");
				sender=prs.getString("sender");
				date=fob.date(prs.getString("datetime"));
				s+=
						"<tr>"+
							"<td>"+
								"<div class=\"btn-group f-right\">"+
									"<button class=\"btnCustom btn-Custom-default b-s-mark\" id=\"read"+messageid+"\" onclick=markAsRead(\""+messageid+"\",\""+id+"\");><i class=\"fa fa-check fa-lg i-green\"></i></button>"+
									"<button class=\"btnCustom btn-Custom-default b-s-reply\" onclick=replyMessage(\""+messageid+"\",\""+sender+"\",\""+id+"\",\"button\");><i class=\"fa fa-reply fa-lg i-blue\"></i></button>"+
									"<button class=\"bbtnCustom btn-Custom-default b-s-delete\" id=\"delete"+messageid+"\" onclick=deleteMessage(\""+messageid+"\",\""+id+"\",\"inbox\");><i class=\"fa fa-close fa-lg i-red\"></i></button>"+
								"</div>"+
								"<a href=\"Profile?id="+sender+"\">@"+sender+"</a> <span class=\"t-grey t-xs\">"+date+"</span></br>"+
								"<span>"+prs.getString("message")+"</span>"+
								"<div id=\"replydiv"+messageid+"\" style=\"display:none;\">"+
									"<span id=\"replyspan"+messageid+"\"></span></br>"+
									"<textarea class=\"form-control\" id=\"replytextarea"+messageid+"\" rows=\"4\" maxlength=\"200\" placeholder=\"reply message\" onkeyup=replyMessage(\""+messageid+"\",\""+sender+"\",\""+id+"\",\"textarea\");></textarea>"+
									"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=replyMessage(\""+messageid+"\",\""+sender+"\",\""+id+"\",\"reply\"); ><i class=\"glyphicon glyphicon-send\"></i> Reply</button></div>"+
								"</div>"+
							"</td>"+
						"</tr>";
			}
			s+=		"</table>";
			
		}catch(Exception e){
			
		}
		return s;
	}
	
	//////////////////////////////////////////////////////////// Inbox read messages // pages inbox, MarkAsReadMessageBbackend, /////////////////////////////////////////////////////////
	
	protected String inboxReadMessages(String id,ResultSet prs){
		
		Functions fob=new Functions();
		String s="",messageid,sender,date;
		boolean valid=true;
		if(fob.validId(id)==false) valid=false;
		if(valid==false) return s;
		
		try{
			prs.beforeFirst();
			s+=		"<table id=\"readtable\" class=\"table table-striped table-hover\">";
			while(prs.next()==true){
				messageid=prs.getString("id");
				sender=prs.getString("sender");
				date=fob.date(prs.getString("datetime"));
				s+=
						"<tr>"+
							"<td>"+
								"<div class=\"btn-group f-right\">"+
									"<button class=\"btnCustom btn-Custom-default b-s-reply\" onclick=replyMessage(\""+messageid+"\",\""+sender+"\",\""+id+"\",\"button\");><i class=\"fa fa-reply fa-lg i-blue\"></i></button>"+
									"<button class=\"btnCustom btn-Custom-default b-s-delete\" id=\"delete"+messageid+"\" onclick=deleteMessage(\""+messageid+"\",\""+id+"\",\"inbox\");><i class=\"fa fa-close fa-lg i-red\"></i></button>"+
								"</div>"+
								"<a href=\"Profile?id="+sender+"\">@"+sender+"</a> <span class=\"t-grey t-xs\">"+date+"</span></br>"+
								"<span>"+prs.getString("message")+"</span>"+
								"<div id=\"replydiv"+messageid+"\" style=\"display:none;\">"+
									"<span id=\"replyspan"+messageid+"\"></span></br>"+
									"<textarea class=\"form-control\" id=\"replytextarea"+messageid+"\" rows=\"4\" maxlength=\"200\" placeholder=\"reply message\" onkeyup=replyMessage(\""+messageid+"\",\""+sender+"\",\""+id+"\",\"textarea\");></textarea>"+
									"<div class=\"b-save\"><button class=\"btn btn-success\" onclick=replyMessage(\""+messageid+"\",\""+sender+"\",\""+id+"\",\"reply\"); ><i class=\"glyphicon glyphicon-send\"></i> Reply</button></div>"+
								"</div>"+
							"</td>"+
						"</tr>";
			}
			s+=		"</table>";
		}catch(Exception e){
		}
		return s;
	}
	
	
	////////////////////////////////////////// fetch tag  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected String[] getTags(String questionid){
		String s="";
		StringBuilder sb=new StringBuilder("");
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			rs=smt.executeQuery("select tag from tags where id in (select tag_id from tag_map where question_id="+questionid+")");
			while(rs.next()==true){
				sb.append(rs.getString("tag")+" ");
			}
			s=sb.toString();
			if(s.length()>0) s=s.substring(0, s.length()-1);
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		return s.split(" ");
	}
	
	////////////////////////////////////////// recent tags ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String recentTags(String id, String page){
		String s="",tag,tagid,icon;
		Functions fob = new Functions();
		if(fob.validId(id) == false) return "";
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement(), smt2=cn.createStatement();
			ResultSet rs,rs2;
			s+=		
						"<table class=\"table table-hover fetchedtable\">"+
							"<tr><th>Recently used tags</th></tr>";
			
			if(page.equals("userhome")) rs=smt.executeQuery("SELECT tags.id, tags.tag, tag_task_map.datetime FROM tags INNER JOIN tag_task_map ON tags.id=tag_task_map.tag_id GROUP BY tags.tag ORDER BY MAX(tag_task_map.datetime) DESC LIMIT 20");		// apply inner join for time base result 	
			else rs=smt.executeQuery("SELECT tags.id, tags.tag, tag_map.datetime FROM tags INNER JOIN tag_map ON tags.id=tag_map.tag_id GROUP BY tags.tag ORDER BY MAX(tag_map.datetime) DESC LIMIT 20");		// apply inner join for time base result 	
			
			while(rs.next()==true){
				tag=rs.getString("tag");if(tag.length()>20) tag=tag.substring(0, 20);
				tagid= rs.getString("id");
				
				rs2 = smt2.executeQuery("select * from tag_user_map where tag_id="+tagid+" and user_id='"+id+"'");
				icon = "<i class=\"fa fa-plus fa-lg i-green\"></i>";
				if(rs2.next() == true) {
					icon = "<i class=\"fa fa-check fa-lg i-grey\"></i>";
				}
				
				if(page.equals("userhome")) s+=	"<tr><td><a href=\"TaskTag?name="+tag+"\"><span class=\"t-bold\">"+tag+"</span></a> <button id=\"maptag"+tagid+"\" class=\"btnCustom btn-Custom-default b-s-mark f-right\" onclick=mapUser(\"tag\",\""+tagid+"\",\"fetch\",\""+page+"\");>"+icon+"</button></td></tr>";
				else s	+=	"<tr><td><a href=\"Tag?name="+tag+"\"><span class=\"t-bold\">"+tag+"</span></a> <button id=\"maptag"+tagid+"\" class=\"btnCustom btn-Custom-default b-s-mark f-right\" onclick=mapUser(\"tag\",\""+tagid+"\",\"fetch\",\""+page+"\");>"+icon+"</button></td></tr>";
			}
			s+=			"</table>";
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		return s;
	}
	////////////////////////////////////////// my tags ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String myTags(String id, String page){
		String s="",tag,tagid,icon;
		int count = 0;
		Functions fob = new Functions();
		if(fob.validId(id) == false) return "";
		if(fob.validName(page) == false) return "";
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement(), smt2=cn.createStatement();
			ResultSet rs,rs2;
			s+=		
					"<table class=\"table table-striped table-hover table-bordered fetchedtable\">"+
							"<tr><th>My Tags</th></tr>"+
							"<tr><td>";
			rs=smt.executeQuery("select * from tags where id in (select tag_id from tag_user_map where user_id = '"+id+"') order by tag asc"); 	
			
			while(rs.next()==true){
				count += 1;
				tag=rs.getString("tag");if(tag.length()>20) tag=tag.substring(0, 20);
				tagid= rs.getString("id");
				
				icon = "<i class=\"fa fa-times t-red fa-lg\"></i>";
				
				s+=			"<span class=\"taglabel t-md t-bold t-italic nowrap\"><a href=\"TaskTag?name="+tag+"\">"+tag+"</a> <a href=\"javascript:void(0)\" id=\"maptag"+tagid+"\" class=\"\" onclick=mapUser(\"tag\",\""+tagid+"\",\"remove\",\""+page+"\");>"+icon+"</a></span> ";
			}
			if(count == 0) {
				s += "<span class=\"t-blue t-bold\">No tags added</span>";
			}
			s+=			"</td></tr></table>";
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		return s;
	}
	////////////////////////////////////////// search tags ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String searchTags(String id, String regex, String page){
		String s="",tag,tagid,icon,st="%";
		int count = 0;
		Functions fob = new Functions();
		regex = regex.trim();
		if(regex.length() == 0) {
			return myTags(id, page);
		}
		if(fob.validId(id) == false) return "";
		if(fob.validTag(regex) == false){
			s+=	"<span class=\"t-red t-bold\">Invalid Tag Name</span>";
			return s;
		}
		if(fob.validName(page) == false) return "";
		
		for(int i = 0; i < regex.length(); i++) {
			st+=regex.charAt(i)+"%";
		}
		regex = st;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement(), smt2=cn.createStatement();
			ResultSet rs,rs2;
			s+=		
					"<table class=\"table table-striped table-hover table-bordered fetchedtable\">"+
							"<tr><th>Similar tags</th></tr>";
			rs=smt.executeQuery("select * from tags where tag like '"+regex+"' order by tag asc"); 	
			
			while(rs.next()==true){
				count += 1;
				tag=rs.getString("tag");if(tag.length()>20) tag=tag.substring(0, 20);
				tagid= rs.getString("id");
				
				rs2 = smt2.executeQuery("select * from tag_user_map where tag_id="+tagid+" and user_id='"+id+"'");
				icon = "<i class=\"fa fa-plus fa-lg i-green\"></i>";
				if(rs2.next() == true) {
					icon = "<i class=\"fa fa-check fa-lg i-grey\"></i>";
				}
				
				s+=			"<tr><td><a href=\"Tag?name="+tag+"\"><span class=\"t-bold\">"+tag+"</span></a> <button id=\"maptag"+tagid+"\" class=\"btnCustom btn-Custom-default b-s-mark f-right\" onclick=mapUser(\"tag\",\""+tagid+"\",\"fetch\",\""+page+"\");>"+icon+"</button></td></tr>";
			}
			s+=			"</table>";
			if(count == 0) {
				s = "<span class=\"t-blue t-bold\">No tags found</span>";
			}
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		return s;
	}
	
	////////////////////////////////////////// send notification ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String sendNotification(HashSet<String> userid,String notification){
		Functions fob=new Functions();
		String s="Failed",user;
		Iterator<String> iterator=userid.iterator();
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			PreparedStatement smt=cn.prepareStatement("insert into notifications values (null,?,1,now(),?)");
			smt.setString(2, notification);
			while(iterator.hasNext()){
				user=iterator.next();
				if(fob.validId(user)==false) continue;
				smt.setString(1, user);
				smt.executeUpdate();
			}
			s="Success";
			smt.close();cn.close();
		}catch(Exception e){}
		return s;
	}
	
	////////////////// domain name /////////////////////////////////////////
	protected String domainName() {
		//return "localhost:8080";
		return "https://deepakbaghel.in";
	}
	
	//////////// Home top ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String head(String id){
		Functions fob=new Functions();
		String s="<!DOCTYPE html>" + 
				"<html lang=\"en\">" + 
					"<head>" + 
						"<title> TMS</title>"+
						"<meta charset=\"utf-8\">"+
						"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" + 
						"<link rel=\"shortcut icon\" href=\"clock.gif\" type=\"image/png\" />"+
						
						// clock
						
						"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css\">" + 
						"<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\">" +
						
						"<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js\"></script>" + // this must be a less prior than bootstrapcdn
						"<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script>" + // must be after jquery.min
						
				//		"<link rel=\"stylesheet\" href=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css\">"+
				//		"<script src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js\"></script>"+
						
				
						
						"<script src=\"script.js\"></script>"+
						"<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" media=\"all\"/>"+
					"</head>" +
				"<body>";
				if(id.length()>0) s += "<div>"+fob.modal(id)+"</div>";
		return s;
	}
	
	///////////////////////////////////////// community navbar ///////////////////////////////////////////////////////////////////////////////////
	protected String navbar(HttpSession ses){
		Functions fob=new Functions();
		boolean valid=true;
		String uid,emailhash,name,navbar,buttons,notificationcount="0";
		uid=ses.getAttribute("user_id").toString();
		emailhash=ses.getAttribute("emailhash").toString();
		name=ses.getAttribute("user_fname").toString();
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			rs=smt.executeQuery("select count(*) as ncount from notifications where status=1 and author='"+uid+"'");
			if(rs.next()==true) notificationcount=rs.getString("ncount");
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		
		if(fob.validId(uid)==false) valid=false;
		if(fob.validName(name)==false) valid=false;
		if(fob.validNumber(notificationcount)==false) valid=false;
		if(valid==false) return "";
		
		if(name.length()>10) name=name.substring(0, 10);
		
		if(notificationcount.equals("0")==false) notificationcount="<sup><span id=\"notificationcount\" class=\"label\">"+notificationcount+"</span></sup>";
		else notificationcount="<sup><span id=\"notificationcount\" class=\"label\" style=\"display:none\">0</span></sup>";
		
		buttons=	"<ul class=\"dropdown-menu menulink\">"+
						"<li><a href=\"Profile?id="+uid+"\"><img class=\"img-circle\" id=\"navbarprofilepic\" alt=\" \" src=\"https://www.gravatar.com/avatar/"+emailhash+"?s=17.jpg\" /> Profile</a></li>"+
						"<li><a href=\"AskedQuestions\">Asked Questions</a></li>"+
						"<li><a href=\"MyAnswers\">My Answers</a></li>"+
						"<li><a href=\"ReadingList\">Reading List</a></li>"+
						"<li class=\"divider\"></li>"+
						"<li class=\"libtm\"><span class=\"lileft\"><a href=\"Message?type=feature\">Request feature</a></span> <span class=\"liright\"><a href=\"Message?type=help\">Help</a></span></li>"+
						"<li class=\"libtm\"><span class=\"lileft\"><a href=\"Message?type=bug\">Report bug</a></span> <span class=\"liright\"><a href=\"Message?type=contact\">Contact Us</a></span></li>"+
						"<li class=\"libtm\"><span class=\"lileft\"><a href=\"Message?type=feedback\">Feedback</a></span> <span class=\"liright\"><a href=\"Logout\">Logout</a></span></li>"+
					"</ul>";
		navbar=
				"<nav class=\"navbar navbar-inverse navbar-fixed-top\" role=\"navigation\">"+
					"<div class=\"container\">"+
						"<div class=\"navbar-header\">"+
							"<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#mynavbar\">"+
								"<span class=\"sr-only\">Toggle navigation</span>"+	
								"<span class=\"icon-bar\"></span>"+
								"<span class=\"icon-bar\"></span>"+
								"<span class=\"icon-bar\"></span>"+
							"</button>"+
							"<span class=\"navbar-brand\"><a href=\"Index\"><img class=\"img-responsive\" src=\"img/tms.png\" alt=\"TMS\" id=\"sitelogo\" /></a></span>"+
						"</div>"+
						"<div class=\"collapse navbar-collapse\" id=\"mynavbar\">"+
							"<ul class=\"nav navbar-nav navbar-left\">"+
								"<li><a href=\"UserHome\"><i class=\"fa fa-home fa-lg\"></i> Home</a></li>"+
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
								"<li><a href=\"Community\"><i class=\"fa fa-users fa-lg\"></i> Community</a></li>"+
								"<li class=\"dropdown\">"+
									"<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"javascript:void(0)\">Hi &nbsp;<span id=\"username\">"+name+"</span> <span class=\"caret\"></span></a>"+ 
									buttons+
								"</li>"+
								"<li>"+
									"<a href=\"javascript:void(0)\" data-toggle=\"popover\" data-trigger=\"click\" data-html=\"true\" data-placement=\"bottom\" data-content='<div id=\"notificationdiv\"><div class=\"t-center\"><i class=\"fa fa-spinner fa-pulse fa-2x t-blue\"></i></div></div>' onclick=notification(\"show\",\""+uid+"\",\"1\");> <span><i class=\"fa fa-bell-o fa-lg\"></i>"+notificationcount+" Notifications</span> </a>"+
								"</li>"+
							"</ul>"+
						"</div>"+
					"</div>"+
				"</nav>";
		return navbar;
	}
	////////////////////////////////////////////////// userhome navbar ///////////////////////////////////////////////////////////////////////////////////////
	protected String navbarHome(HttpSession ses){
		Functions fob=new Functions();
		boolean valid=true;
		String uid,emailhash,name,navbar,buttons,notificationcount="0";
		uid=ses.getAttribute("user_id").toString();
		emailhash=ses.getAttribute("emailhash").toString();
		name=ses.getAttribute("user_fname").toString();
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			rs=smt.executeQuery("select count(*) as ncount from notifications where status=1 and author='"+uid+"'");
			if(rs.next()==true) notificationcount=rs.getString("ncount");
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		
		if(fob.validId(uid)==false) valid=false;
		if(fob.validName(name)==false) valid=false;
		if(fob.validNumber(notificationcount)==false) valid=false;
		if(valid==false) return "";
		
		if(name.length()>10) name=name.substring(0, 10);
		
		if(notificationcount.equals("0")==false) notificationcount="<sup><span id=\"notificationcount\" class=\"label\">"+notificationcount+"</span></sup>";
		else notificationcount="<sup><span id=\"notificationcount\" class=\"label\" style=\"display:none\">0</span></sup>";
		
		buttons="<ul class=\"dropdown-menu menulink\">"+
				"<li><a href=\"Inbox\">Inbox</a></li>"+
				"<li><a href=\"Outbox\">Outbox</a></li>"+
				"<li><a href=\"UserAccountSetting\">Account settings</a></li>"+
				"<li><a href=\"SearchUser\">Search User</a></li>"+
				"<li><a href=\"Notes\">My Plans</a></li>"+
				"<li class=\"divider\"></li>"+
				"<li class=\"libtm\"><span class=\"lileft\"><a href=\"Message?type=feature\">Request feature</a></span> <span class=\"liright\"><a href=\"Message?type=help\">Help</a></span></li>"+
				"<li class=\"libtm\"><span class=\"lileft\"><a href=\"Message?type=bug\">Report bug</a></span> <span class=\"liright\"><a href=\"Message?type=contact\">Contact Us</a></span></li>"+
				"<li class=\"libtm\"><span class=\"lileft\"><a href=\"Message?type=feedback\">Feedback</a></span> <span class=\"liright\"><a href=\"Logout\">Logout</a></span></li>"+
				"</ul>";
		
		navbar=
				"<nav class=\"navbar navbar-inverse navbar-fixed-top\" role=\"navigation\">"+
					"<div class=\"container\">"+
						"<div class=\"navbar-header\">"+
							"<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#mynavbar\">"+
								"<span class=\"sr-only\">Toggle navigation</span>"+	
								"<span class=\"icon-bar\"></span>"+
								"<span class=\"icon-bar\"></span>"+
								"<span class=\"icon-bar\"></span>"+
							"</button>"+
							"<span class=\"navbar-brand\"><a href=\"Index\"><img class=\"img-responsive\" src=\"img/tms.png\" alt=\"TMS\" id=\"sitelogo\" /></a></span>"+
						"</div>"+
						"<div class=\"collapse navbar-collapse\" id=\"mynavbar\">"+
							"<ul class=\"nav navbar-nav navbar-left\">"+
								
								"<li><a href=\"Profile?id="+uid+"\"><img class=\"img-circle\" id=\"navbarprofilepic\" alt=\" \" src=\"https://www.gravatar.com/avatar/"+emailhash+"?s=17.jpg\" /> My Profile</a></li>"+
								"<li><a href=\"UserHome\"><i class=\"fa fa-home fa-lg\"></i> Home</a></li>"+
								"<li><a href=\"Community\"><i class=\"fa fa-users\"></i> Community</a></li>"+
							"</ul>"+
							"<ul class=\"nav navbar-nav navbar-right\">"+
								"<li class=\"dropdown\">"+
									"<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"javascript:void(0)\">Hi &nbsp;<span id=\"username\">"+name+"</span> <span class=\"caret\"></span></a>"+ 
									buttons+
								"</li>"+
								"<li>"+
									"<a href=\"javascript:void(0)\" data-toggle=\"popover\" data-trigger=\"click\" data-html=\"true\" data-placement=\"bottom\" data-content='<div id=\"notificationdiv\"><div class=\"t-center\"><i class=\"fa fa-spinner fa-pulse fa-2x t-blue\"></i></div></div>' onclick=notification(\"show\",\""+uid+"\",\"1\");> <span><i class=\"fa fa-bell-o fa-lg\"></i>"+notificationcount+" Notifications</span> </a>"+
								"</li>"+
								"<li>"+
									"<a href=\"Logout\"><i class=\"fa fa-sign-out fa-lg\"></i> Logout</a></span></li>"+								
								"</li>"+
							"</ul>"+
						"</div>"+
					"</div>"+
				"</nav>";
		return navbar;
	}
	////////////////////////////////////////////////// Admin navbar ///////////////////////////////////////////////////////////////////////////////////////
	
	protected String navbarAdmin(HttpSession ses){
		Functions fob=new Functions();
		boolean valid=true;
		
		String id=ses.getAttribute("admin_id").toString(),Home,buttons,notificationcount="0";
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			rs=smt.executeQuery("select count(*) as ncount from notifications where status=1 and author='"+id+"'");
			if(rs.next()==true) notificationcount=rs.getString("ncount");
			rs.close();smt.close();cn.close();
		}catch(Exception e){}
		
		if(fob.validId(id)==false) valid=false;
		if(fob.validNumber(notificationcount)==false) valid=false;
		if(valid==false) return "";
		
		if(notificationcount.equals("0")==false) notificationcount="<sup><span id=\"notificationcount\" class=\"label\">"+notificationcount+"</span></sup>";
		else notificationcount="<sup><span id=\"notificationcount\" style=\"display:none\" class=\"label\">0</span></sup>";
		
		buttons=
				"<ul class=\"dropdown-menu menulink\">"+
						"<li><a href=\"Profile?id="+id+"\">Profile</a></li>"+
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
						"<li><a href=\"RecycleBin\">Recycle Bin</a></li>"+
						"<li><a href=\"AdminMessage\">Messages</a></li>"+
						"<li><a href=\"Logout\"><i class=\"fa fa-sign-out fa-lg\"></i> Logout</a></li>"+
					"</ul>";
		
		Home=
					"<nav class=\"navbar navbar-inverse navbar-fixed-top\" role=\"navigation\">"+
						"<div class=\"container\">"+
							"<div class=\"navbar-header\">"+
								"<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#mynavbar\">"+
									"<span class=\"sr-only\">Toggle navigation</span>"+	
									"<span class=\"icon-bar\"></span>"+
									"<span class=\"icon-bar\"></span>"+
									"<span class=\"icon-bar\"></span>"+
								"</button>"+
								"<span class=\"navbar-brand\"><a href=\"Index\"><img class=\"img-responsive\" src=\"img/tms.png\" alt=\"TMS\" id=\"sitelogo\" /></a></span>"+
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
										"<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"javascript:void(0)\">Hi &nbsp;<span id=\"username\">Admin</span> <span class=\"caret\"></span></a>"+ 
										buttons+
									"</li>"+
									"<li>"+
										"<a href=\"javascript:void(0)\" data-toggle=\"popover\" data-trigger=\"click\" data-html=\"true\" data-placement=\"bottom\" data-content='<div id=\"notificationdiv\"><div class=\"t-center\"><i class=\"fa fa-spinner fa-pulse fa-2x t-blue\"></i></div></div>' onclick=notification(\"show\",\""+id+"\",\"1\");> <span><i class=\"fa fa-bell-o fa-lg\"></i>"+notificationcount+" Notifications</span> </a>"+
									"</li>"+
								"</ul>"+
							"</div>"+
						"</div>"+
					"</nav>";
		return Home;
	}
	
//////////////////////////////////////////////////guest navbar ///////////////////////////////////////////////////////////////////////////////////////
	
	protected String navbarGuest(){
		Functions fob=new Functions();
		boolean valid=true;
		String uid,emailhash,name,Home,buttons;
		
		Home=
				"<nav class=\"navbar navbar-inverse navbar-fixed-top\" role=\"navigation\">"+
					"<div class=\"container\">"+
						"<div class=\"navbar-header\">"+
							"<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#mynavbar\">"+
								"<span class=\"sr-only\">Toggle navigation</span>"+	
								"<span class=\"icon-bar\"></span>"+
								"<span class=\"icon-bar\"></span>"+
								"<span class=\"icon-bar\"></span>"+
							"</button>"+
							"<span class=\"navbar-brand\"><a href=\"Index\"><img class=\"img-responsive\" height=50 src=\"img/tms.png\" alt=\"TMS\" id=\"sitelogo\" /></a></span>"+
						"</div>"+
						"<div class=\"collapse navbar-collapse\" id=\"mynavbar\">"+
							"<ul class=\"nav navbar-nav navbar-left\">"+
								"<li><a href=\"javascript:void(0)\">Hi &nbsp;<span id=\"username\">Guest</span></a></li>"+ 
								"<li><a href=\""+fob.domainName()+"/TMS/SignUp\"></a></li>"+
							"</ul>"+
							"<ul class=\"nav navbar-nav navbar-right\">"+
								"<li>"+
									"<a href=\""+fob.domainName()+"/TMS/Login\"><i class=\"fa fa-sign-in fa-lg\"></i> Login</a>"+
								"</li>"+
								"<li>"+
								"<a href=\""+fob.domainName()+"/TMS/SignUp\"><i class=\"fa fa-chevron-circle-up fa-lg\"></i> Signup</a>"+
								"</li>"+
							"</ul>"+
						"</div>"+
					"</div>"+
				"</nav>";
		return Home;
	}
	
	/////////////////////////////////////////////////////  modal ////////////////////////////////////////////////////////////////////////////////////////
	
	protected String modal(String id){
		String s;
		s=
				"<div class=\"modal fade\" id=\"modal\" role=\"dialog\">"+
					"<div class=\"modal-dialog\">"+
						"<div class=\"modal-content\" id=\"modal-content\">"+
							"<div class=\"modal-header\">"+
								"<button type=\"button\" class=\"close\" data-dismiss=\"modal\">&times;</button>"+
								"<h4 class=\"modal-title\">Search on TMS</h4>"+
							"</div>"+
							"<div class=\"modal-body\">"+
								"<form id=\"modalform\" class=\"navbar-form\" action=\"SearchOrAskQuestion\" method=\"post\">"+
									"<div class=\"input-group form-group\">"+
										"<input id=\"searchaskinput\" list=\"predictlist\" autocomplete=\"off\" type=\"text\" class=\"form-control\" size=\"100\" maxlength=\"200\" name=\"question\" required=\"required\" autofocus onkeyup=predict(); pattern=\".{10,200}\" title=\"only 10-200 characters\" placeholder=\" Search or Ask query here\"></br></br></br>"+
										"<datalist id=\"predictlist\"></datalist>"+
										"<input type=\"text\" id=\"tag\" name=\"tag\" size=\"100\" maxlength=\"200\" class=\"form-control input-sm\" placeholder=\"tags separated by space\">"+
									"</div></br></br>"+
									"<div id=\"hiddendiv\" style=\"display:none\"></div>"+ // to check question length is changed or not
									"<button style=\"border:0px;background-color:transparent;\" id=\"searchbutton\" name=\"searchquestion\" value=\"search\"></button>"+
									"<button style=\"border:0px;background-color:transparent;\" id=\"askbutton\" name=\"askquestion\" value=\"ask\"></button>"+
								"</form>"+
							"</div>"+
							"<div class=\"modal-footer\">"+
								"<button class=\"btn btn-info\" onclick=searchOrAskQuestion(\""+id+"\",\"searchbutton\");><i class=\"glyphicon glyphicon-search\"></i> Search</button>"+
								"<button class=\"btn btn-default\" onclick=searchOrAskQuestion(\""+id+"\",\"askbutton\");>Ask</button>"+
							"</div>"+
						"</div>"+
					"</div>"+
				"</div>";
		return s;
	}
	
	///////////// recommended task  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String recommendedTasks(String id, String who, String page,String panel) {
		Functions fob=new Functions();
		String s="",Q,t,imp;
		int count = 0;
		boolean valid=true;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			
			
			Q="SELECT * FROM task "
					+ "INNER JOIN tag_task_map "
						+ "ON task.id = tag_task_map.task_id "
					+ "INNER JOIN tag_user_map "
						+ "ON tag_task_map.tag_id = tag_user_map.tag_id AND tag_user_map.user_id = '"+id+"' "
					+ "WHERE date >= CURDATE() "
						+ "AND task.privacy = 'public' "
						+ "AND task.author != tag_user_map.user_id "
						+ "AND task.id NOT IN (SELECT task_id FROM task_user_map WHERE user_id = '"+id+"')"
					+ "GROUP BY task.id "
					+ "ORDER BY COUNT(*) DESC "
					+ "LIMIT 20";

			
			rs=smt.executeQuery(Q);
			s+="<table class=\"table table-hover table-bordered fetchedtable\">"+
					"<tr><th>Recommended</th></tr>";
			while(rs.next()==true){
				t=fob.formTask(rs, id, who, page, panel);
				if(t.equals("Failed")==true||t.length()==0) continue;
				imp = "";
				if(rs.getString("type").equals("important") == true) {
					imp = "imptask";
				}
				s+=
						"<tr class=\""+imp+"\">"+
							"<td>"+
								t+
							"</td>"+
						"</tr>";
				count++;
			}
			s+="</table>";
			rs.close();smt.close();cn.close();
			if(count == 0){
				s = "<span class=\"t-bold t-blue\">No recommendations for you <i class=\"fa fa-frown-o fa-lg\"></i></span>";
			}
		}catch(Exception e){
			s="Failed";
		}
		return s;
	}
	
	
	///////////// recommended task  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String tasksFollowed(String id, String who, String page,String panel) {
		Functions fob=new Functions();
		String s="",Q,t,imp;
		int count = 0;
		boolean valid=true;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q = "select * from task where privacy='public' and date >= curdate() and author !='"+id+"' and id in (select task_id from task_user_map where user_id='"+id+"') order by date desc";

			rs=smt.executeQuery(Q);
			s+="<table class=\"table table-hover table-bordered fetchedtable\">"+
					"<tr><th>Following</th></tr>";
			while(rs.next()==true){
				t=fob.formTask(rs, id, who, page, panel);
				if(t.equals("Failed")==true||t.length()==0) continue;
				imp = "";
				if(rs.getString("type").equals("important") == true) {
					imp = "imptask";
				}
				s+=
						"<tr class=\""+imp+"\">"+
							"<td>"+
								t+
							"</td>"+
						"</tr>";
				count++;
			}
			s+="</table>";
			rs.close();smt.close();cn.close();
			if(count == 0){
				s = "<span class=\"t-bold t-blue\">No task followed</span>";
			}
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	
	
	///////////// search task  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String searchTasks(String id, String regex, String who, String page, String panel) {
		Functions fob=new Functions();
		String s="",Q,t,st="%",imp;
		int count = 0;
		boolean valid=true;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		regex = regex.trim();
		if(regex.length() == 0) {
			return recommendedTasks(id, who, page, "recommended");
		}
		for(int i = 0; i < regex.length(); i++) {
			st+=regex.charAt(i)+"%";
		}
		regex = st;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			Q = "select * from task where heading like '"+regex+"' order by date desc";

			rs=smt.executeQuery(Q);
			s+="<table class=\"table table-hover table-bordered fetchedtable\">"+
					"<tr><th>Search Results</th></tr>";
			while(rs.next()==true){
				t=fob.formTask(rs, id, who, page, panel);
				if(t.equals("Failed")==true||t.length()==0) continue;
				imp = "";
				if(rs.getString("type").equals("important") == true) {
					imp = "imptask";
				}
				s+=
						"<tr class=\""+imp+"\">"+
							"<td>"+
								t+
							"</td>"+
						"</tr>";
				count++;
			}
			s+="</table>";
			rs.close();smt.close();cn.close();
			if(count == 0){
				s = "<span class=\"t-bold t-red\">No matches found</span>";
			}
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	///////////// form task  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String formTask(ResultSet rs, String id, String who, String page, String panel) {
		String s="",taskid,heading,description, imp, type;
		try {
			taskid = rs.getString("id");
			s+=			"<div>"+
							"<span class=\"t-grey t-bold\">"+dateRev(rs.getString("date"))+" </span>"+
							"<span class=\"f-right\">";
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement();
				ResultSet rs2;
				rs2 = smt.executeQuery("select * from task_user_map where task_id="+taskid+" and user_id ='"+id+"'");
				if(rs2.next() == true) {
					s+= 	"<a href=\"javascript:void(0)\" id=\"maptask"+taskid+"\" onclick=mapUser(\"task\",\""+taskid+"\",\"remove\",\""+page+"\",\""+panel+"\");><i class=\"fa fa-minus-circle fa-lg i-orange\"></i></a>&nbsp;&nbsp;&nbsp;";
				} else {
					s+= 	"<a href=\"javascript:void(0)\" id=\"maptask"+taskid+"\" onclick=mapUser(\"task\",\""+taskid+"\",\"add\",\""+page+"\",\""+panel+"\");><i class=\"fa fa-plus-circle fa-lg i-green\"></i></a>&nbsp;&nbsp;&nbsp;";
				}
				rs2.close();
			}catch(Exception e){}
			if(equals(rs.getString("author"), id) == true || equals(who, "admin") == true)
				s+=				"<a href=\"TaskEdit?taskid="+taskid+"\"><i class=\"fa fa-edit fa-lg i-blue\"></i></a>&nbsp;&nbsp;&nbsp;"+
								"<a href=\"javascript:void(0)\" id=\"deletetask"+taskid+"\" onclick=deleteTask(\""+taskid+"\",\""+page+"\",\""+panel+"\");><i class=\"fa fa-trash fa-lg i-red\"></i></a>";
			imp = "";
			type = rs.getString("type");
			if(type.equals("important") == true) {
				imp = " impheading";
			}
			heading = rs.getString("heading");
			description = rs.getString("description");
			if(description.length() > 50) description = description.substring(0, 50)+" ...";
			s+=				"</span>"+
						"</div>"+
						"<div class=\"clearfix\"></div>"+
						"<div class=\"t-center t-italic\">"+
							"<a href=\"Task?id="+taskid+"\"><h6 class=\"t-bold"+imp+"\">"+heading+"</h6></a>"+
						"</div>"+
						"<div>"+
							"<span class=\"t-grey t-xs\">"+description+"</span>"+
						"</div>";			
			
		}catch(Exception e) {
			s = "Failed";
		}
		return s;
	}

	
	///////////// fetch task  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String fetchTask(String id, String tab, String page, String who, String panel) {
		Functions fob=new Functions();
		String s="",Q,t,imp;
		int count = 0;
		boolean valid=true;
		if(fob.validId(id)==false) valid=false;
		if(fob.validName(tab) == false) valid = false;
		if(fob.validName(page)==false) valid=false;
		if(valid==false) return s;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			Statement smt=cn.createStatement();
			ResultSet rs;
			if(tab.equals("upcoming") == true) {
				Q = "select * from task where (date >= curdate() and id in (select task_id from task_user_map where user_id = '"+id+"')) order by date asc"; // check for the server time it may vary 
			} else if(tab.equals("newest") == true) {
				Q = "select * from task where id in (select task_id from task_user_map where user_id = '"+id+"') order by date desc";
			} else if(tab.equals("oldest") == true) {
				Q = "select * from task where id in (select task_id from task_user_map where user_id = '"+id+"') order by date asc";
			} else {
				Q = "select * from task where author = '"+id+"' order by date desc";
			}
			
			rs=smt.executeQuery(Q);
			s+="<table class=\"table table-hover table-bordered\">";
			
			while(rs.next()==true){
				t=fob.getTask(rs, page, id, who, panel);
				if(t.equals("Failed")==true||t.length()==0) continue;
				imp = "";
				if(rs.getString("type").equals("important") == true) {
					imp = "imptask";
				}
				s+=
						"<tr class=\""+imp+"\">"+
							"<td>"+
								t+
							"</td>"+
						"</tr>";
				count++;
			}
			s+="</table>";
			rs.close();smt.close();cn.close();
			if(count == 0) s = "";
		}catch(Exception e){
			s="Failed";
		}
		
		return s;
	}
	///////////// get task  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected String getTask(ResultSet rs, String page, String id, String who, String panel) {
		String s="",taskid,privacy,type,imp,pub;
		try {
			taskid = rs.getString("id");
			s+=
						"<div class=\"f-right\">";
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
				Statement smt=cn.createStatement();
				ResultSet rs2;
				rs2 = smt.executeQuery("select * from task_user_map where task_id="+taskid+" and user_id ='"+id+"'");
				if(rs2.next() == true) {
					s+= 	"<a href=\"javascript:void(0)\" id=\"maptask"+taskid+"\" onclick=mapUser(\"task\",\""+taskid+"\",\"remove\",\""+page+"\",\""+panel+"\");><i class=\"fa fa-minus-circle fa-lg i-orange\"></i></a>&nbsp;&nbsp;&nbsp;";
				} else {
					s+= 	"<a href=\"javascript:void(0)\" id=\"maptask"+taskid+"\" onclick=mapUser(\"task\",\""+taskid+"\",\"add\",\""+page+"\",\""+panel+"\");><i class=\"fa fa-plus-circle fa-lg i-green\"></i></a>&nbsp;&nbsp;&nbsp;";
				}
				rs2.close();
			}catch(Exception e){}
			if(equals(rs.getString("author"), id) == true || equals(who, "admin") == true)
				s+=			"<a href=\"TaskEdit?taskid="+taskid+"\"><i class=\"fa fa-edit fa-lg i-blue\"></i></a>&nbsp;&nbsp;&nbsp;"+
							"<a href=\"javascript:void(0)\" id=\"deletetask"+taskid+"\" onclick=deleteTask(\""+taskid+"\",\""+page+"\",\""+panel+"\");><i class=\"fa fa-trash fa-lg i-red\"></i></a>";
			
			imp = ""; pub = "<i class=\"fa fa-user fa-lg i-grey\"></i>";
			type = rs.getString("type");
			privacy = rs.getString("privacy");
			if(type.equals("important") == true) {
				imp = " impheading";
			}
			if(privacy.equals("public") == true) {
				pub = "<i class=\"fa fa-users fa-lg i-grey\"></i>";
			}
			
			s+=			"</div>"+
						"<div class=\"clearfix\"></div>"+
						"<div class=\"t-center t-italic\">"+
							"<h4 class=\"t-bold"+imp+"\">"+rs.getString("heading")+"</h4>"+
						"</div>"+
						"<div>"+
							"<span>"+rs.getString("description")+"</span>"+
						"</div>"+
						"<div class=\"f-right\">"+
							"<span class=\"t-bold t-grey\"> "+dateRev(rs.getString("date"))+" </span>&nbsp;"+pub+
						"</div>";			
			
		}catch(Exception e) {
			s = "Failed";
		}
		return s;
	}
	
	
	///////////// insert words  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String footer(){
		String s;
		s=					"<div style=\"height:100px\"></div>"+
							"<footer style=\"\" class=\"container-fluid text-center bg-4\">"+
							"<div>"+
								"</br>"+
								"<span class=\"t-bold\">Quick links: </span>"+
								"<a href=\"Message?type=feature\">Request feature</a>&nbsp;&nbsp;"+
								"<a href=\"Message?type=help\">Help </a>&nbsp;&nbsp;"+
								"<a href=\"Message?type=bug\">Report </a>&nbsp;&nbsp;"+
								"<a href=\"Message?type=contact\">Contact us</a>&nbsp;&nbsp;"+
								"<a href=\"Message?type=feedback\">Feedback </a>&nbsp;&nbsp;"+
								"</br></br></br></br>"+
							"</div>"+
						"</footer>";
		
		return s;
	}
	
	
	
	///////////// insert words  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected void insertWords(String id, String text){
		Functions fob=new Functions();
		String sb[]=text.split(" "),s=null;
		int l=sb.length,i,len;
		if(fob.validId(id)==false) return;
		
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection cn=DriverManager.getConnection("jdbc:mysql://localhost:3306/deepakba_TMS","deepakba_user","Nl]G;T*}lX6+");
			PreparedStatement psmt1,psmt2,psmt3,psmt4,psmt5,psmt6;
			ResultSet rs;
			psmt1=cn.prepareStatement("select id from words where word=?");
			psmt2=cn.prepareStatement("update words set used=used+1 where word=?");
			psmt3=cn.prepareStatement("insert into words values (null,'"+id+"',?,1,now())");
			psmt4=cn.prepareStatement("select id from word_predict where first=? and second=?");
			psmt5=cn.prepareStatement("update word_predict set used=used+1 where first=? and second=?");
			psmt6=cn.prepareStatement("insert into word_predict values (null,?,?,1)");
			
			for(i=0;i<l;i++){
				len=sb[i].length();
				if(len==0||len>45||len==1&&fob.onlyAlphabet(sb[i])==0){
					continue;
				}
				if(sb[i].charAt(len-1)=='.'||sb[i].charAt(len-1)==','||sb[i].charAt(len-1)=='?') sb[i]=sb[i].substring(0, len-1);
				cn.setAutoCommit(false);
				try{
					psmt1.setString(1, sb[i]);
					rs=psmt1.executeQuery();
					if(rs.next()==true){
						psmt2.setString(1, sb[i]); psmt2.executeUpdate();
					} else{
						psmt3.setString(1, sb[i]); psmt3.executeUpdate();
					}
					if(s!=null){
						psmt4.setString(1, s); psmt4.setString(2, sb[i]); rs=psmt4.executeQuery();
						if(rs.next()==true){
							psmt5.setString(1, s); psmt5.setString(2, sb[i]); psmt5.executeUpdate();
						} else{
							psmt6.setString(1, s); psmt6.setString(2, sb[i]); psmt6.executeUpdate();
						}
					}
					cn.commit();rs.close();
				}catch(Exception e){}
				cn.rollback();
				s=sb[i];
			}
			psmt1.close();psmt2.close();psmt3.close();psmt4.close();psmt5.close();psmt6.close();cn.close();
			
		}catch(Exception e){}
	}
	
	///////////// validation ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	protected boolean equals(String a, String b){
		try{
			if(a.equals(b) == true) return true;
		}catch(Exception e){}
		return false;
	}
	protected boolean equalsIgnoreCase(String a, String b){
		try{
			if(a.toLowerCase().equals(b.toLowerCase()) == true) return true;
		}catch(Exception e){}
		return false;
	}
	
	protected boolean validText(String a){
		boolean b=true;
/*		try{
			int l=a.length();
			for(int i=0;i<l;i++){
				if(a.charAt(i)=='\'') b=false;
			}
		}catch(Exception e){
			b=false;
		}
*/		return b;
	}
	
	protected String parseDate(String a){
		boolean b=true;
		int i;
		String date[] = new String[0], s="";
		a = a.trim();
		for(i = 0; i < a.length(); i++) {
			if((a.charAt(i) >= '0' && a.charAt(i) <= '9') == false) {
				date = a.split(a.charAt(i)+"");
			}
		}
		for(i = 0; i < date.length; i++) {
			if(validNumber(date[i]) == false) {
				b = false;
			}
		}
		if(b==false || date.length != 3) {
			return "";
		}
		
		if(date[2].length() == 4) {
			String st = date[2];
			date[2] = date[0];
			date[0] = st;
		}
		if(date[2].length() == 1) {
			date[2] = "0"+date[2];
		}
		if(date[1].length() == 1) {
			date[1] = "0"+date[1];
		}
		try {
			DateFormat sourceFormat = new SimpleDateFormat("yyyy/MM/dd");
			sourceFormat.setLenient(false);
			String dateAsString = date[0]+"/"+date[1]+"/"+date[2];
			Date date1 = sourceFormat.parse(dateAsString);
			Date date2 = new Date();
			if(date2.compareTo(date1) > 0) {
				return "";
			}
		}catch(Exception e){
			return "";
		}
		return date[0]+"-"+date[1]+"-"+date[2];
	}
	
	protected boolean validLink(String a){
		boolean b=false;
		try{
			int l=a.length(),count=0;
			char c;
			if(l==0) return false;
			for(int i=0;i<l;i++){
				c=a.charAt(i);
				if(c>='0'&&c<='9'||c>='A'&&c<='Z'||c>='a'&&c<='z'||c=='.'||c==':'||c=='/'||c=='-'||c=='_'||c=='@'||c=='#') count++;
			}
			if(l==count) b=true;
		}catch(Exception e){}
		return b;
	}
	
    protected boolean validTag(String tag){
    	boolean b=false;
    	try{
    		int c = 0;
    		char ch;
    		c += (noOfAlphabet(tag) + noOfDigit(tag));
        	for(int i=0;i<tag.length();i++){
        		ch = tag.charAt(i);
        		if(ch == '-' || ch == '_') c++; 
        	}
        	if(c == tag.length()&&tag.length()>0&&tag.length()<=45) b = true;
		}catch(Exception e){}
    	return b;
    }

    protected String[] parseTag(String tag){		// only alphanumeric, '-' and '_' 
    	
    	String s[]=tag.toLowerCase().split(" ");
    	int sl=0,idx;
    	tag="";
    	for(int i=0;i<s.length;i++){ // removing duplicates
    		for(int j=i+1;j<s.length;j++){
    			if(s[i].equals(s[j])==true) s[j]="";
    		}
    	}
    	
    	for(int i=0;i<s.length;i++){ // valid tag only
    		if(validTag(s[i]) == true) s[sl++]=s[i];
    	}
    	for(int i=0;i<sl;i++){
    		tag+=s[i];
    		if(i!=sl-1) tag+=" ";
    	}
    	
    	return tag.split(" ");
    }
    
    protected boolean validId(String id){
    	boolean b=false;
		try{
	    	reg="^[a-zA-Z_][a-zA-Z0-9_]{0,20}$";
	    	pat=Pattern.compile(reg);
	    	mat=pat.matcher(id);
	    	b= mat.matches();
		}catch(Exception e){}
		if(!(id.length()>0&&id.length()<=20)) b=false;
		return b;
    }
    protected boolean validName(String name){
    	boolean b=false;
		try{
			if(onlyAlphabet(name)==1&&name.length()>0&&name.length()<=20) b=true;
		}catch(Exception e){}
    	return b;
    }
    protected boolean validNumber(String num){
		try{
			if(onlyDigit(num)==1&&num.length()>0) return true;
		}catch(Exception e){}
    	return false;
    }
    protected boolean validPassword(String password){
		try{
			if(noOfSpecialChar(password)+noOfDigit(password)+noOfAlphabet(password)==password.length()&&password.length()>0&&password.length()<=45) return true;
		}catch(Exception e){}    	
    	return false;
    }
	protected boolean validEmail(String email){
		boolean b=false;
		try{
			int c=0;
			String st[]=email.split("@");
			if(email.length()>4&&st.length==2&&st[0]!=""&&st[1]!=""&&email.length()<=45){
				b=true;
			}
			for(int i=0;i<email.length();i++){
				if(email.charAt(i)=='.') c=1;
				if(email.charAt(i)=='\''){
					c=0;break;
				}
			}
			if(c==0) b=false;
		}catch(Exception e){}    	
		return b;
	}
    protected boolean validAlphaNumeric(String a){
		try{
			if(noOfAlphabet(a)+noOfDigit(a)==a.length()) return true;
		}catch(Exception e){}
    	return false;
    }
    protected boolean validKey(String a){
    	boolean b=false;
		try{
		   	if(noOfAlphabet(a)+noOfDigit(a)==a.length()&&a.length()==50) b=true;
	    	for(int i=0;i<a.length();i++){
	    		if(a.charAt(i)>='A'&&a.charAt(i)<='Z'){
	    			b=false;break;
	    		}
	    	}
		}catch(Exception e){}    	
    	return b;
    }
    
    
    
    
    ///////////////////////////////////////////////////////////////////////////////////// Utility functions//////////////////////////////////////////////
    
	protected int isAlphabet(String a){
		try{
			int l=a.length();
			for(int i=0;i<l;i++){
				if((a.charAt(i)>=97&&a.charAt(i)<=122)||(a.charAt(i)>=65&&a.charAt(i)<=90)){
					return 1;
				}
			}
		}catch(Exception e){}
		return 0;
	}
	
	protected int noOfAlphabet(String a){
		int l=a.length();
		int c=0;
		try{
			for(int i=0;i<l;i++)
				if((a.charAt(i)>=97&&a.charAt(i)<=122)||(a.charAt(i)>=65&&a.charAt(i)<=90))
					c++;
		}catch(Exception e){}

		return c;
	}
	
	protected int onlyAlphabet(String a){
		try{
			if(a.length()==noOfAlphabet(a)) return 1;
		}catch(Exception e){}
	
		return 0;
	}

	protected int isDigit(String a){
		try{
			int l=a.length();
			for(int i=0;i<l;i++){
				if(a.charAt(i)>=48&&a.charAt(i)<=57){
					return 1;
				}
			}
		}catch(Exception e){}

		return 0;
	}
	protected int noOfDigit(String a){
		int l=a.length();
		int c=0;
		try{
			for(int i=0;i<l;i++)
				if(a.charAt(i)>=48&&a.charAt(i)<=57)
					c++;
		}catch(Exception e){}

		return c;
	}
	protected int onlyDigit(String a){
		try{
			if(a.length()==noOfDigit(a)) return 1;
		}catch(Exception e){}
	
		return 0;
	}

	protected int isSpecialChar(String a){
		try{
			int i, l=a.length();
			for(i=0;i<l;i++){
				if((a.charAt(i)>=35&&a.charAt(i)<=38)||(a.charAt(i)>=42&&a.charAt(i)<=46)||(a.charAt(i)>=58&&a.charAt(i)<=64)||a.charAt(i)==95){
					return 1;
				}
			}
		}catch(Exception e){}
	
		return 0;
	}

	protected int noOfSpecialChar(String a){
		int c=0,i, l=a.length();
		try{
			for(i=0;i<l;i++)
				if((a.charAt(i)>=35&&a.charAt(i)<=38)||(a.charAt(i)>=42&&a.charAt(i)<=46)||(a.charAt(i)>=58&&a.charAt(i)<=64)||a.charAt(i)==95)
					c++;
		}catch(Exception e){}

		return c;
	}
	protected int onlySpecialChar(String a){
		try{
			if(a.length()==noOfSpecialChar(a)) return 1;
		}catch(Exception e){}
	
		return 0;
	}
}
    //////////////  used in search or ask question ////////////////////////////////////
class MergeSort { // Decreasing order
	private int[] array,temp;
    private int[] array2,temp2;
    private int length;
 
    protected void sort(int inputArr[], int a[],int b,int e) {
        this.array = inputArr;
        this.array2=a;
        this.length = inputArr.length;
        this.temp = new int[length];
        this.temp2= new int[length];
        mergeSort(b, e - 1);
    }
 
    private void mergeSort(int low, int high) {
        if (low < high) {
            int mid = high+low>>1;
            mergeSort(low, mid);
            mergeSort(mid + 1, high);
            merge(low, mid, high);
        }
    }

    private void merge(int low, int mid, int high) {
        int i = low;
        int j = mid + 1;
        int k = low;
        while (i <= mid && j <= high) {
            if (array[i] <= array[j]) {
                temp[k] = array[j];temp2[k++]=array2[j++];
            } else {
            	temp[k] = array[i];temp2[k++]=array2[i++];
            }
        }
        while (i <= mid) {
        	temp[k] = array[i];temp2[k++]=array2[i++];
        }
        while (j <= high) {
        	temp[k] = array[j];temp2[k++]=array2[j++];
        }
        for(i=low;i<=high;i++){
        	array[i]=temp[i];array2[i]=temp2[i];
        }
    }
}