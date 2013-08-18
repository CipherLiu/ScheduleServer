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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Servlet implementation class GroupSocial
 */
public class GroupSocial extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection groupCollection; 
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupSocial() {
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
		String groupId = request.getParameter("groupId");
		String dateTimeInMillis = request.getParameter("dateTimeInMillis");

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong(dateTimeInMillis));
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);

		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			DBCollection socialCollection = scheduleDB.getCollection("social_"+userId);
			groupCollection = scheduleDB.getCollection("group_" + userId);
			DBObject memberQuery = new BasicDBObject();
			memberQuery.put("_id", new ObjectId(groupId));
			DBCursor groupCur = groupCollection.find(memberQuery);
			if(groupCur.hasNext()){
				String memberString = groupCur.next().get("member").toString();
				JSONArray members = new JSONArray(memberString);
				JSONArray socialArray = new JSONArray();
				for(int i = 0; i < members.length(); i++){
					DBObject socialQuery = new BasicDBObject();
					String memberId = members.getString(i);
					socialQuery.put("userId", memberId);
					DBCursor socialCur = socialCollection.find(socialQuery);
					JSONArray eventArray = new JSONArray();
					while(socialCur.hasNext()){
						DBObject socialObject = socialCur.next();
						String eventId = socialObject.get("eventId").toString();
						DBCollection eventCollection = 
								scheduleDB.getCollection("event_"+memberId);
						DBObject eventQuery = new BasicDBObject();
						eventQuery.put("_id",new ObjectId(eventId));

						BasicDBObject conditionOne = new BasicDBObject();
						BasicDBObject conditionTwo = new BasicDBObject();
						Date dateBegin = new Date();
						Date dateEnd = new Date();
						dateBegin.setTime(cal.getTimeInMillis());
						conditionTwo.put("$gte", dateBegin);
						cal.roll(Calendar.DAY_OF_YEAR, true);
						dateEnd.setTime(cal.getTimeInMillis());
						conditionOne.put("$lte", dateEnd);
						
						eventQuery.put("calFrom",conditionOne);
						eventQuery.put("calTo",conditionTwo);
						DBCursor eventCur = eventCollection.find(eventQuery);
						JSONObject eventJSONObject = new JSONObject();
						if(eventCur.hasNext()){
							DBObject eventObject = eventCur.next();
							SocialInfo socialInfo = new SocialInfo();
							socialInfo.eventId = eventObject.get("_id").toString();
							socialInfo.from = (Date)eventObject.get("calFrom");
							socialInfo.to = (Date)eventObject.get("calTo");
							eventJSONObject.put("eventId", socialInfo.eventId);
							eventJSONObject.put("calFrom", socialInfo.from.getTime());							
							eventJSONObject.put("calTo", socialInfo.to.getTime());
							eventArray.put(eventJSONObject);
						}
						cal.roll(Calendar.DAY_OF_YEAR, false);
					}
					JSONObject socialJSONObject = new JSONObject();
					socialJSONObject.put("eventArray", eventArray);
					DBCollection userCollection = scheduleDB.getCollection("user");
					DBObject userQuery = new BasicDBObject();
					userQuery.put("_id", new ObjectId(memberId));
					DBCursor userCur = userCollection.find(userQuery);
					String memberImage = "";
					if(userCur.hasNext()){
						DBObject userObject = userCur.next();
						memberImage = userObject.get("image").toString();
					}
					socialJSONObject.put("memberId", memberId);
					socialJSONObject.put("memberImage", memberImage);
					socialArray.put(socialJSONObject);
				}
				jb.put("result", Primitive.ACCEPT);
				jb.put("socialArray", socialArray);
			}
		}catch(MongoException e){
			try {
				jb.put("result", Primitive.DBCONNECTIONERROR);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		   	e.printStackTrace();
		}catch(JSONException e){
			e.printStackTrace();
		}catch(NullPointerException npe){
			JSONArray socialArray = new JSONArray();
			try {
				jb.put("result", Primitive.ACCEPT);
				jb.put("socialArray", socialArray);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	
	
	class SocialInfo{
		public Date from;
		public Date to;
		public String eventId;
		SocialInfo(){
			from = new Date();
			to = new Date();
			eventId = "";
		}
	}
}
