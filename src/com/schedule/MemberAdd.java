/*
 * add members to the group located by groupId
 * */
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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

/**
 * Servlet implementation class MemberAdd
 */
public class MemberAdd extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MemberAdd() {
        super();
        // TODO Auto-generated constructor stub
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
			String groupId = receivedObject.getString("groupId");
			try{
				Mongo mongo =new Mongo();
				DB scheduleDB = mongo.getDB("schedule");
				DBCollection groupCollection = scheduleDB.getCollection("group_"+userId);
				int i;
				for(i =0; i < friends.length(); i++){				
					DBObject memberCheckQuery = new BasicDBObject();
					memberCheckQuery.put("member", friends.getString(i));
					memberCheckQuery.put("_id", new ObjectId(groupId));
					DBCursor mcqCur = groupCollection.find(memberCheckQuery);
					if(!mcqCur.hasNext()){
						DBObject updateObject = new BasicDBObject();
						DBObject member = new BasicDBObject();
						member.put("member", friends.getString(i));
						updateObject.put("$push", member);
						DBObject updateQuery = new BasicDBObject();
						updateQuery.put("_id", new ObjectId(groupId));WriteResult wr2 =groupCollection.update(
								updateQuery, updateObject);
						if(wr2.getN() == 0){
							jb.put("result", Primitive.DBSTOREERROR);
							break;
						}
					}
				}
				if( i == friends.length()){
					jb.put("result", Primitive.ACCEPT);
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
