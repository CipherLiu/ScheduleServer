/*
 *Add new members to the default group "All friends"
 * 
 **/

package com.schedule;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * Servlet implementation class GroupUpdate
 */
public class GroupUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		JSONObject jb = new JSONObject();
		InputStreamReader reader = 
				new InputStreamReader(request.getInputStream(), "utf-8"); 
		String receivedString = "";
		char[] buff = new char[1024]; 
		int length = 0; 
		while ((length = reader.read(buff)) != -1) 
		{ 
			receivedString = new String(buff, 0, length); 
		}
		try {
			JSONObject receivedObject = new JSONObject(receivedString);
			String userId = receivedObject.getString("userId");
			JSONArray friends = receivedObject.getJSONArray("friends");
			try{
				Mongo mongo =new Mongo();
				DB scheduleDB = mongo.getDB("schedule");
				DBCollection groupCollection = scheduleDB.getCollection("group_"+userId);
				for(int i = 0; i < friends.length(); i++){
					DBObject ifExist = new BasicDBObject();
					ifExist.put("groupName", "All friends");
					ifExist.put("member", friends.getString(i));
					if(!groupCollection.find(ifExist).hasNext()){
						DBObject friend = new BasicDBObject();
						String memberId = friends.getString(i);
						friend.put("member", memberId);
						DBObject member = new BasicDBObject();
						member.put("$push", friend);
						DBObject query = new BasicDBObject();
						query.put("groupName", "All friends");
						groupCollection.update(query,member);
						DBCollection eventCollection = scheduleDB.getCollection("event_"+userId);
						DBCursor eventCur = eventCollection.find();
						int count = 0;
						while(eventCur.hasNext() && count <10){
							DBObject eventObject = eventCur.next();
							String eventId = eventObject.get("_id").toString();
							String updateTime = eventObject.get("updateTime").toString();
							DBObject socialObject = new BasicDBObject();
							socialObject.put("userId", userId);
							socialObject.put("eventId", eventId);
							socialObject.put("updateTime", updateTime);
							DBCollection socialCollection = 
									scheduleDB.getCollection("social_"+memberId);
							socialCollection.save(socialObject);
						}
						
					}			
				}
			}catch(MongoException e){
				jb.put("result", Primitive.DBCONNECTIONERROR);
				e.printStackTrace();
			}
			jb.put("result", Primitive.ACCEPT);
		} 
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrintWriter writer = response.getWriter();
		writer.write(jb.toString());
		writer.flush();
		writer.close();
	}

}
