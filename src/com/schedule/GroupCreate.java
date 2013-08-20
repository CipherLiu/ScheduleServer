/*
 *Create a new group
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

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * Servlet implementation class GroupCreate
 */
public class GroupCreate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupCreate() {
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
			String groupName = receivedObject.getString("groupName");
			try{
				Mongo mongo =new Mongo();
				DB scheduleDB = mongo.getDB("schedule");
				DBCollection groupCollection = scheduleDB.getCollection("group_"+userId);
				DBObject groupDBObject = new BasicDBObject();
				groupDBObject.put("groupName", groupName);
				WriteResult wr = groupCollection.save(groupDBObject);
				if(wr.getN() != 0){
					jb.put("result", Primitive.DBSTOREERROR);
				}else{
					int i;
					for(i =0; i < friends.length(); i++){
						DBObject updateObject = new BasicDBObject();
						DBObject member = new BasicDBObject();
						member.put("member", friends.getString(i));
						updateObject.put("$push", member);
						DBObject updateQuery = new BasicDBObject();
						String groupId = groupDBObject.get("_id").toString();
						updateQuery.put("_id", new ObjectId(groupId));
						WriteResult wr2 =groupCollection.update(
								updateQuery, updateObject);
						if(wr2.getN() == 0){
							jb.put("result", Primitive.DBSTOREERROR);
							break;
						}
					}
					if( i == friends.length()){
						jb.put("result", Primitive.ACCEPT);
						jb.put("groupId", groupDBObject.get("_id").toString());
					}
				}
				
			}catch(MongoException e){
				jb.put("result", Primitive.DBCONNECTIONERROR);
				e.printStackTrace();
			}
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
