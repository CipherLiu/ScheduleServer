/*
 *Check user's event in one day 
 * 
 **/

package com.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
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
 * Servlet implementation class OneDayEventQuery
 */
public class OneDayEventQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection eventCollection;
    private Date dateToQuery = new Date();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OneDayEventQuery() {
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
		//String username = new String(request.getParameter("username").getBytes("ISO-8859-1"),"UTF-8"); 
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			eventCollection = scheduleDB.getCollection("event_" + userId);

			BasicDBObject eventCheckQuery = new BasicDBObject();
			BasicDBObject conditionOne = new BasicDBObject();
			BasicDBObject conditionTwo = new BasicDBObject();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(dateToQuery.getTime());
			Date dateBegin = new Date();
			Date dateEnd = new Date();
			
			dateBegin.setTime(cal.getTimeInMillis());
			conditionTwo.put("$gte", dateBegin);
			cal.roll(Calendar.DAY_OF_YEAR, true);
			dateEnd.setTime(cal.getTimeInMillis());
			conditionOne.put("$lte", dateEnd);
				
			eventCheckQuery.put("calFrom",conditionOne);
			eventCheckQuery.put("calTo",conditionTwo);
			DBCursor cur = eventCollection.find(eventCheckQuery);
			JSONArray ja = new JSONArray();
			while(cur.hasNext()){
				//ja.add(cur.next());
				JSONObject eventJSONObject= new JSONObject();
				DBObject dbo = cur.next();
				String id = dbo.get("_id").toString();
				String eventName = (String)dbo.get("eventName");
				Date calFrom = (Date)dbo.get("calFrom");
				Date calTo = (Date)dbo.get("calTo");
				String locationName = (String)dbo.get("locationName");
				String locationCoordinate = (String)dbo.get("locationCoordinate");
				String decription = (String)dbo.get("decription");
				String photo = (String)dbo.get("photo");
				String record = (String)dbo.get("record");
				int commentCount = (Integer)dbo.get("commentCount");
				String commentsString;
				try{
					commentsString = dbo.get("comments").toString();
					eventJSONObject.put("commentsString", commentsString);
				}catch(NullPointerException npe){
					JSONArray nullArray = new JSONArray();
					eventJSONObject.put("commentsString", nullArray.toString());
				}
				String updateTime = (String)dbo.get("updateTime");
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
				eventJSONObject.put("publisherId",userId);
				DBCollection userCollection = scheduleDB.getCollection("user");
				DBObject imageQuery = new BasicDBObject();
				imageQuery.put("_id", new ObjectId(userId));
				DBCursor cur3 = userCollection.find(imageQuery);
				DBObject dbo3 = cur3.next();
				String image = (String)dbo3.get("image");
				String publisherName = (String)dbo3.get("username");
				eventJSONObject.put("publisherImage",image);
				eventJSONObject.put("publisherName",publisherName);
				ja.add(eventJSONObject);
			}
			jb.put("result", Primitive.ACCEPT);
			jb.put("eventArray", ja);
			
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
