/*
 *Check the groups' info  
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
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Servlet implementation class GroupCheck
 */
public class GroupCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection groupCollection; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupCheck() {
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

			DBCursor cur = groupCollection.find();
			JSONArray ja = new JSONArray();
			while(cur.hasNext()){
				//ja.add(cur.next());
				JSONObject groupJSONObject= new JSONObject();
				DBObject dbo = cur.next();
				String id = dbo.get("_id").toString();
				String groupName = (String)dbo.get("groupName");
				groupJSONObject.put("_id", id);
				groupJSONObject.put("groupName", groupName);
				ja.add(groupJSONObject);
			}
			jb.put("result", Primitive.ACCEPT);
			jb.put("groupList",ja);
			
		}catch(MongoException e){
			jb.put("result", Primitive.DBCONNECTIONERROR);
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
