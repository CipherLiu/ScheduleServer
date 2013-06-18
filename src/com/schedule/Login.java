package com.schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import com.mongodb.DB;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import com.mongodb.DBCursor;
//import com.mongodb.util.JSON;
/**
 * Servlet implementation class Login
 */
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Mongo connection;
    private DB scheduleDB;
    private DBCollection userCollection;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
//		 
//		response.setContentType("text/html;charset=gb2312");
//	    PrintWriter out = response.getWriter();
//	    out.println("<HTML>");
//	    out.println("<HEAD>");
//	    out.println("<TITLE>Hello Servlet</TITLE>");
//	    out.println("</HEAD>");
//	    out.println("<BODY>");
//	    out.println("<B>Hello, World !</B>");
//	    out.println("</BODY>");
//	    out.println("</HTML>");
//	    out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	try {  
              
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(),"utf-8"));  
            String line = "";  
            StringBuffer buf = new StringBuffer();  
            while ( (line = br.readLine()) != null ) {  
                buf.append(line);  
            }
            br.close();
            JSONObject param = JSON.parseObject(buf.toString());
            //JSONObject param = new JSONObject(buf.toString());
            String email = param.getString("username");
            String password = param.getString("password");
            //String eMail = param.getString("email");
            JSONObject jb =new JSONObject();
            try{
            	connection = new Mongo();
            	scheduleDB = connection.getDB("schedule");
            	userCollection = scheduleDB.getCollection("user");
            	BasicDBObject loginQuery = new BasicDBObject();
                loginQuery.put("email", email);
                DBCursor cur = userCollection.find(loginQuery);
                if(!cur.hasNext()){
                	    jb.put("result", Primitive.USERUNREGISTERED);
                }else{
                	loginQuery.put("password", password);
                    cur = userCollection.find(loginQuery);
                    if(!cur.hasNext()){
                    	jb.put("result", Primitive.WRONGPASSWORD);
                    }else{
                    	DBObject result = cur.next();
                    	String userId = result.get("_id").toString();
                    	jb.put("result", Primitive.ACCEPT);
                    	jb.put("userId", userId);
                    }
                }
                
            }catch(MongoException e){
            	    jb.put("result", Primitive.DBCONNECTIONERROR);
            	    e.printStackTrace();
            }
        PrintWriter writer = response.getWriter();
    	writer.write(jb.toString());
    	writer.flush();
    	writer.close();
		}catch (IOException e) {  
            e.printStackTrace();  
        }
	
	}
}
