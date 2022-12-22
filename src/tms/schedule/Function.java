package tms.schedule;

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
 * Servlet implementation class Function
 */
@WebServlet("/Function")
class Function extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    protected Function() {
        super();
        // TODO Auto-generated constructor stub
    }
    private Pattern pat;
	private Matcher mat;
	private String reg;
	
    /////////////// generate key ////////////////////////////
    private SecureRandom random = new SecureRandom();
    protected String nextKey() {
    	String s;
    	while((s=new BigInteger(250, random).toString(32)).length()!=50)
    		s=new BigInteger(250, random).toString(32);
      return s;
    }

	////////////////// domain name /////////////////////////////////////////
	protected String domainName() {
		//return "http://localhost:8080";
		return "https://deepakbaghel.in";
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
	

	protected String sendNotification(HashSet<String> userid,String notification){
		Function fob=new Function();
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
	
	
	//////////////////////////////////////////////////////////// Send mail /////////////////////////////////////////////////////////////////////////////////////////////
	
	protected String sendMail(final String from,final String password,String to,String subject, String content){
		Function fob=new Function();
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
    
    
    
    
    ///////////////////////////////////////////////////////////////////////////////////// Utility Function//////////////////////////////////////////////
    
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