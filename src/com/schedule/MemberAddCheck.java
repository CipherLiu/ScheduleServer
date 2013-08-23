/*
 *Add new members to group located by groupId
 * 
 **/
package com.schedule;

import java.io.IOException;
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

/**
 * Servlet implementation class MemberAddCheck
 */
public class MemberAddCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MemberAddCheck() {
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
		String groupId = request.getParameter("groupId");
		Mongo mongo = new Mongo();
		DB scheduleDB = mongo.getDB("schedule");
		DBCollection groupCollection = scheduleDB.getCollection("group_"+userId);		
		DBObject groupQuery = new BasicDBObject();
		groupQuery.put("groupName", "All friends");
		DBCursor defGroupCur = groupCollection.find(groupQuery);
		if(defGroupCur.hasNext()){
			DBObject tarGroupDBObject = defGroupCur.next();
			try {
				String defGroupMemberString = tarGroupDBObject.get("member").toString();
				JSONArray defGroupMemberArray = new JSONArray(defGroupMemberString);
				JSONArray memberExcludeArray = new JSONArray();
				for(int i = 0; i < defGroupMemberArray.length(); i++){
					String memberId = defGroupMemberArray.get(i).toString();
					DBObject tarGroupQuery = new BasicDBObject();
					tarGroupQuery.put("_id", new ObjectId(groupId));
					tarGroupQuery.put("member", memberId);
					DBCursor tarGroupCur = groupCollection.find(tarGroupQuery);
					if(!tarGroupCur.hasNext()){
						DBCollection userCollection = scheduleDB.getCollection("user");
						DBObject userQuery = new BasicDBObject();
						userQuery.put("_id", new ObjectId(memberId));
						DBCursor userCur = userCollection.find(userQuery);
						if(userCur.hasNext()){
							DBObject newMemberObject = userCur.next();
							String memberName = (String)newMemberObject.get("username");
							String memberImage = (String)newMemberObject.get("image");
							JSONObject stragerObject = new JSONObject();
							stragerObject.put("memberId", memberId);
							stragerObject.put("memberName", memberName);
							stragerObject.put("memberImage", memberImage);
							memberExcludeArray.put(stragerObject);
						}else{
							jb.put("result", Primitive.DBCONNECTIONERROR);
							break;
						}
					}
				}
				jb.put("result", Primitive.ACCEPT);
				jb.put("memberExcludeArray", memberExcludeArray);
			} catch(NullPointerException npe){
				
				JSONArray memberExcludeArray = new JSONArray();
				try {
					jb.put("result", Primitive.ACCEPT);
					jb.put("memberExcludeArray", memberExcludeArray);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				jb.put("result", Primitive.DBCONNECTIONERROR);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		String jbString = new String(jb.toString().getBytes(),"UTF-8");
		writer.write(jbString);
		writer.flush();
		writer.close();
	}

}
