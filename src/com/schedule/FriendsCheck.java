package com.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
 * Servlet implementation class FriendsCheck
 */
public class FriendsCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection groupCollection;    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FriendsCheck() {
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
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			groupCollection = scheduleDB.getCollection("group_" + userId);
			DBObject friendsQuery = new BasicDBObject();
			friendsQuery.put("groupName", "All friends");
			DBCursor cur = groupCollection.find(friendsQuery);
			JSONArray friendsArray = new JSONArray();
			JSONArray friends = new JSONArray();
			while(cur.hasNext()){
				DBObject dbo = cur.next();
				try {
					friends = new JSONArray(dbo.get("member").toString());
					for(int i =0; i <friends.length(); i++){
						DBCollection userCollection = scheduleDB.getCollection("user");
						DBObject userQuery = new BasicDBObject();
						String friendId = (String)friends.get(i);
						userQuery.put("_id", new ObjectId(friendId));
						DBCursor userCur = userCollection.find(userQuery);
						if(userCur.hasNext()){
							DBObject friendDBObject = userCur.next();
							JSONObject friendJSONObject = new JSONObject();
							String friendName = (String)friendDBObject.get("username");
							String friendImage = (String)friendDBObject.get("image");
							friendJSONObject.put("friendId", friendId);
							friendJSONObject.put("friendName", friendName);
							friendJSONObject.put("friendImage", friendImage);
							friendsArray.put(friendJSONObject);
						}
					}
					jb.put("friendsArray", friendsArray);
					jb.put("result", Primitive.ACCEPT);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}catch(MongoException e){
			try {
				jb.put("result", Primitive.DBCONNECTIONERROR);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		   	e.printStackTrace();
		}
		PrintWriter writer = response.getWriter();
		writer.write(jb.toString());
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
