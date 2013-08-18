package com.schedule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bson.types.ObjectId;

//import org.apache.tomcat.util.http.fileupload.FileItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * Servlet implementation class Register
 */
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection userCollection; 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
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
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String username = new String(request.getParameter("username").getBytes("ISO-8859-1"),"UTF-8");
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			userCollection = scheduleDB.getCollection("user");

			BasicDBObject registerQuery = new BasicDBObject();
			registerQuery.put("email", email);
			DBCursor cur = userCollection.find(registerQuery);
			if(cur.hasNext()){
				jb.put("result", Primitive.USERREGISTERED);
			}else{
				DBObject user = new BasicDBObject();
				
				user.put("email", email);
				user.put("password", password);
				user.put("username", username);
				user.put("image", "null");
				user.put("eventCount", 0);
				user.put("groupCount", 1);
				if(userCollection.save(user).getN() != 0){
					jb.put("result", Primitive.DBSTOREERROR);
				}else{
					jb.put("result", Primitive.ACCEPT);
					DBObject group = new BasicDBObject();
					//DBObject myself = new BasicDBObject();
					group.put("groupName","All friends");
					//myself.put("groupName","myself");
					DBCollection groupCollection = 
							scheduleDB.getCollection("group_" + user.get("_id"));
					groupCollection.save(group);
					//groupCollection.save(myself);
				}
			}
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
		JSONObject jb =new JSONObject();
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {  
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			try {
				connection = new Mongo();
				scheduleDB = connection.getDB("schedule");
				List items = upload.parseRequest(request);
				Iterator iter = items.iterator();  
				ObjectId userObjectId = new ObjectId();
				String userId = userObjectId.toString();
				while (iter.hasNext()) {  
					FileItem item = (FileItem) iter.next();
					if (item.isFormField()) {  
						String paramValue = item.getString(); 
						JSONObject param = JSON.parseObject(paramValue);
						String email = param.getString("email");
						String password = param.getString("password");
						String username = param.getString("username");
						String image = param.getString("image");
						userCollection = scheduleDB.getCollection("user");
						BasicDBObject registerQuery = new BasicDBObject();
						registerQuery.put("email", email);
						DBCursor cur = userCollection.find(registerQuery);
						if(cur.hasNext()){
							jb.put("result", Primitive.USERREGISTERED);
						}else{
							DBObject user = new BasicDBObject();
							user.put("_id", userObjectId);
							user.put("email", email);
							user.put("password", password);
							user.put("username", username);
							user.put("image", image.substring(
									0, image.length() - 4)+userId+".jpg");
							user.put("eventCount", 0);
							user.put("groupCount", 1);
							if(userCollection.save(user).getN() != 0){
								jb.put("result", Primitive.DBSTOREERROR);
							}else{
								jb.put("result", Primitive.ACCEPT);
								DBObject group = new BasicDBObject();
								//DBObject myself = new BasicDBObject();
								group.put("groupName","All friends");
								//myself.put("groupName","myself");
								DBCollection groupCollection = 
										scheduleDB.getCollection("group_" + user.get("_id"));
								groupCollection.save(group);
								//groupCollection.save(myself);
							}
						}	 
					} else {  
						String fileName = item.getName(); 
						fileName = fileName.substring(0, fileName.length() - 4)+userId+".jpg";
						byte[] data = item.get();
						InputStream inputStream = new ByteArrayInputStream(data);
						GridFS fs = new GridFS(scheduleDB, "userimg");
						GridFSInputFile fsFile = fs.createFile(inputStream);
						fsFile.setFilename(fileName);
						fsFile.setContentType("image/jpg");
						fsFile.save();
						inputStream.close();
					}	
				}  
			} catch (FileUploadException e) {  
				jb.put("request", Primitive.FILEPARSEERROR);
				e.printStackTrace();  
			}catch(MongoException e){
			   	jb.put("result", Primitive.DBCONNECTIONERROR);
			   	e.printStackTrace();
			}
			PrintWriter writer = response.getWriter();
			writer.write(jb.toString());
			writer.flush();
			writer.close();	
		}
	}
}
