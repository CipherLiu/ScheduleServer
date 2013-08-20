package com.schedule;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import com.mongodb.WriteResult;

/**
 * Servlet implementation class CommentUpdate
 */
public class CommentUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CommentUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
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
			String publisherId = receivedObject.getString("publisherId");
			String commentContent = receivedObject.getString("commentContent");
			String eventId = receivedObject.getString("eventId");
			String publishTime = receivedObject.getString("publishTime");
			try{
				Mongo mongo =new Mongo();
				DB scheduleDB = mongo.getDB("schedule");
				DBCollection eventCollection = scheduleDB.getCollection("event_"+publisherId);
				DBObject eventQuery = new BasicDBObject();
				eventQuery.put("_id", new ObjectId(eventId));
				DBObject commentAdd = new BasicDBObject();
				
				DBCollection userCollection = scheduleDB.getCollection("user");
				DBObject userQuery = new BasicDBObject();
				userQuery.put("_id", new ObjectId(userId));
				DBCursor userCur = userCollection.find(userQuery);
				if(userCur.hasNext()){
					DBObject userObject = userCur.next();
					String publisherName = userObject.get("username").toString();
					String publisherImage = userObject.get("image").toString();
					commentAdd.put("publisherName", publisherName);
					commentAdd.put("publisherImage", publisherImage);
					commentAdd.put("publisherId", userId);
					commentAdd.put("commentContent", commentContent);
					ObjectId commentId = new ObjectId();
					commentAdd.put("_id",commentId.toString());
					commentAdd.put("publishTime", publishTime);
					DBObject comment = new BasicDBObject();
					comment.put("comments", commentAdd);
					DBObject commentPush = new BasicDBObject();
					commentPush.put("$push", comment);
					WriteResult wr = eventCollection.update(eventQuery, commentPush,true,true);
					if(wr.getN() == 0){
						jb.put("result", Primitive.DBSTOREERROR);
					}else{
						DBObject updateCommentCount = new BasicDBObject();
						DBObject countIncrease = new BasicDBObject();
						countIncrease.put("commentCount", 1);
						updateCommentCount.put("$inc", countIncrease);
						WriteResult wr2 =  eventCollection.update(eventQuery,
								updateCommentCount);
						if(wr2.getN()!=0){
							JSONObject commentJSONObject = new JSONObject();
							commentJSONObject.put("_id", commentId.toString());
							commentJSONObject.put("publisherName", publisherName);
							commentJSONObject.put("publisherImage", publisherImage);
							commentJSONObject.put("publisherId", publisherId);
							commentJSONObject.put("commentContent", commentContent);
							commentJSONObject.put("publishTime", publishTime);
							jb.put("result", Primitive.ACCEPT);
							jb.put("comment", commentJSONObject);
						}else{
							jb.put("result", Primitive.DBSTOREERROR);
						}
					}
				}else{
					jb.put("result", Primitive.DBSTOREERROR);
					
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
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		String jbString = new String(jb.toString().getBytes(),"UTF-8");
		writer.write(jbString);
		writer.flush();
		writer.close();
	}

}
