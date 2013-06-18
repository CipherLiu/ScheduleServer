package com.schedule;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Servlet implementation class Init
 */
public class Init extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection userCollection;   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Init() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject jb = new JSONObject();
		request.setCharacterEncoding("UTF-8");
		String email = request.getParameter("email");
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			userCollection = scheduleDB.getCollection("user");
			BasicDBObject registerQuery = new BasicDBObject();
			registerQuery.put("email", email);
			DBCursor cur = userCollection.find(registerQuery);
			DBObject dbObject = cur.next();
			jb.put("result", Primitive.ACCEPT);
			jb.put("email", dbObject.get("email"));
			jb.put("password", dbObject.get("password"));
			jb.put("image", dbObject.get("image"));
			String tempNickname = (String)dbObject.get("nickname");
			jb.put("nickname", tempNickname);

			}catch(MongoException e){
				jb.put("result", Primitive.DBCONNECTIONERROR);
			   	e.printStackTrace();
			}
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.write(jb.toString());
		writer.flush();
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
