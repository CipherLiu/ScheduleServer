package com.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;

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
 * Servlet implementation class SocialCheck
 */
public class SocialCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection socialCollection;
    private Date dateToQuery = new Date();   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SocialCheck() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject jb = new JSONObject();
		String userId = request.getParameter("userId");
		String dateTimeInMillis = request.getParameter("dateTimeInMillis");
		dateToQuery.setTime(Long.parseLong(dateTimeInMillis));
		//String username = 
		//new String(request.getParameter("username").getBytes("ISO-8859-1"),"UTF-8"); 
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			socialCollection = scheduleDB.getCollection("social_" + userId);
			DBObject orderBy = new BasicDBObject();
			orderBy.put("updateTime", -1);
			DBCursor cur = socialCollection.find().sort(orderBy);
			JSONArray ja = new JSONArray();
			int i = 0;
			while(cur.hasNext() && i < 10){
				//ja.add(cur.next());
				JSONObject eventJSONObject= new JSONObject();
				DBObject dbo = cur.next();
				String publisherId = (String)dbo.get("userId");
				String publishedEventId = (String)dbo.get("eventId");
				DBCollection publisherEventCollection = 
						scheduleDB.getCollection("event_"+publisherId);
				DBObject eventQuery = new BasicDBObject();
				eventQuery.put("_id", new ObjectId(publishedEventId));
				DBCursor cur2 = publisherEventCollection.find(eventQuery);
				DBObject dbo2 = cur2.next();
				String id = dbo.get("_id").toString();
				String eventName = (String)dbo2.get("eventName");
				Date calFrom = (Date)dbo2.get("calFrom");
				Date calTo = (Date)dbo2.get("calTo");
				String locationName = (String)dbo2.get("locationName");
				String locationCoordinate = (String)dbo2.get("locationCoordinate");
				String decription = (String)dbo2.get("decription");
				String photo = (String)dbo2.get("photo");
				String record = (String)dbo2.get("record");
				int commentCount = (Integer)dbo2.get("commentCount");
				String updateTime = (String)dbo2.get("updateTime");
				eventJSONObject.put("_id", id);
				eventJSONObject.put("eventName", eventName);
				eventJSONObject.put("calFrom", calFrom.getTime());
				eventJSONObject.put("calTo", calTo.getTime());
				eventJSONObject.put("locationName", locationName);
				eventJSONObject.put("locationCoordinate", locationCoordinate);
				eventJSONObject.put("decription", decription);
				eventJSONObject.put("photo", photo);
				eventJSONObject.put("record", record);
				eventJSONObject.put("commentCount", commentCount);
				eventJSONObject.put("updateTime", updateTime);
				eventJSONObject.put("publisherId",publisherId);
				DBCollection userCollection = scheduleDB.getCollection("user");
				DBObject imageQuery = new BasicDBObject();
				imageQuery.put("_id", new ObjectId(publisherId));
				DBCursor cur3 = userCollection.find(imageQuery);
				if(cur3.hasNext()){
					DBObject dbo3 = cur3.next();
					String image = (String)dbo3.get("image");
					eventJSONObject.put("publisherImage",image);
				}else{
					eventJSONObject.put("publisherImage","null");
				}
				ja.add(eventJSONObject);
				i++;
			}
			jb.put("result", Primitive.ACCEPT);
			jb.put("socialEventArray", ja);
			
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
