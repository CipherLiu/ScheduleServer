/*
 *Check the strangers that can be added as friends
 * 
 **/

package com.schedule;

import java.io.IOException;
import java.io.PrintWriter;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Servlet implementation class StrangerCheck
 */
public class StrangerCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection userCollection;   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StrangerCheck() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject jb = new JSONObject();
		String userId = request.getParameter("userId");
//		String eventName = new String(
//				request.getParameter("eventName").getBytes("ISO-8859-1"),"UTF-8"); 
//		String calFrom = request.getParameter("calFrom");
//		String calTo = request.getParameter("calTo");
//		String locationName = new String(
//				request.getParameter("locationName").getBytes("ISO-8859-1"),"UTF-8");
//		String locationCoordinate = new String(
//				request.getParameter("locationCoordinate").getBytes("ISO-8859-1"),"UTF-8");
//		String decription = new String(
//				request.getParameter("decription").getBytes("ISO-8859-1"),"UTF-8"); 
//		String updateTime = request.getParameter("updateTime");
//		String targetGroup = request.getParameter("targetGroup");
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			userCollection = scheduleDB.getCollection("user");
			DBCursor cur = userCollection.find();
			JSONArray strangersArray = new JSONArray();
			int i = 0;

			while(cur.hasNext() && i <20){
				DBObject stranger = cur.next();
				String strangerId= stranger.get("_id").toString();

				//strangersArray.add(stragerObject);
				DBCollection groupCollection = scheduleDB.getCollection("group_" + userId);
				DBObject groupQuery = new BasicDBObject();
				groupQuery.put("groupName", "All friends");
				groupQuery.put("member", strangerId);
				DBCursor cur2 = groupCollection.find(groupQuery);
				if(!cur2.hasNext() && !strangerId.contentEquals(userId)){
					String strangerName = (String)stranger.get("username");
					String strangerImage = (String)stranger.get("image");
					JSONObject stragerObject = new JSONObject();
					stragerObject.put("strangerId", strangerId);
					stragerObject.put("strangerName", strangerName);
					stragerObject.put("strangerImage", strangerImage);
					strangersArray.add(stragerObject);
				}
				i++;
			}
			jb.put("result", Primitive.ACCEPT);
			jb.put("strangersArray", strangersArray);
			
		}catch(MongoException e){
			jb.put("result", Primitive.DBCONNECTIONERROR);
		   	e.printStackTrace();
		}
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		String jbString = new String(jb.toString().getBytes(),"UTF-8");
		writer.write(jbString);
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
