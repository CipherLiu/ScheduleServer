package com.schedule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * Servlet implementation class EventUpdate
 */
public class EventUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection eventCollection;
    private String photo ;
	private String record;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EventUpdate() {
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
		String eventName = new String(
				request.getParameter("eventName").getBytes("ISO-8859-1"),"UTF-8"); 
		String calFrom = request.getParameter("calFrom");
		String calTo = request.getParameter("calTo");
		String locationName = new String(
				request.getParameter("locationName").getBytes("ISO-8859-1"),"UTF-8");
		String locationCoordinate = request.getParameter("locationCoordinate");
		String decription = new String(
				request.getParameter("decription").getBytes("ISO-8859-1"),"UTF-8"); 
		String updateTime = request.getParameter("updateTime");
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			eventCollection = scheduleDB.getCollection("event_"+userId);
			WriteResult writeResult;
			DBObject event = new BasicDBObject();
			//event.put("userId", userId);
			event.put("eventName", eventName);
			Date dateFrom = new Date();
			Date dateTo = new Date();
			dateFrom.setTime(Long.parseLong(calFrom));
			dateTo.setTime(Long.parseLong(calTo));
			event.put("calFrom", dateFrom);
			event.put("calTo", dateTo);
			event.put("locationName", locationName);
			event.put("locationCoordinate", locationCoordinate);
			event.put("decription", decription);
			event.put("photo", "null");
			event.put("record", "null");
			event.put("commentCount", 0);
			event.put("updateTime", updateTime);
			
			writeResult = eventCollection.save(event);
			if(writeResult.getN() != 0){
				jb.put("result", Primitive.DBSTOREERROR);
			}else{
				jb.put("result", Primitive.ACCEPT);
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
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					if (item.isFormField()) { 
						String paramValue = item.getString(); 
						JSONObject param = JSON.parseObject(paramValue);
						String userId = param.getString("userId");
						String eventName = param.getString("eventName");
						String calFrom = param.getString("calFrom");
						String calTo = param.getString("calTo");
						String locationName = param.getString("locationName");
						String locationCoordinate = param.getString("locationCoordinate");
						String decription = param.getString("decription");
						String updateTime = param.getString("updateTime");
						photo = param.getString("photo");
						record = param.getString("record");
						eventCollection = scheduleDB.getCollection("event_"+userId);
						WriteResult writeResult;
						DBObject event = new BasicDBObject();
						event.put("updateTime", updateTime);
						event.put("eventName", eventName);
						event.put("calFrom", calFrom);
						event.put("calTo", calTo);
						event.put("locationName", locationName);
						event.put("locationCoordinate", locationCoordinate);
						event.put("decription", decription);
						event.put("photo", photo.isEmpty() ? "null" : photo);
						event.put("record", record.isEmpty() ? "null" : record);
						event.put("commentCount", 0);
						writeResult = eventCollection.save(event);
						if(writeResult.getN() != 0){
							jb.put("result", Primitive.DBSTOREERROR);
						}else{
							jb.put("result", Primitive.ACCEPT);
						}
							 
					} else {
						String fileName = item.getName();
						if(fileName.startsWith("EventImage")){
							byte[] data = item.get();
							InputStream inputStream = new ByteArrayInputStream(data);
							GridFS fs = new GridFS(scheduleDB, "eventimg");
							GridFSInputFile fsFile = fs.createFile(inputStream);
							fsFile.setFilename(fileName);
							fsFile.setContentType("image/jpg");
							fsFile.save();
							inputStream.close();	
						}else if(fileName.startsWith("EventRecord")){
							byte[] data = item.get();
							InputStream inputStream = new ByteArrayInputStream(data);
							GridFS fs = new GridFS(scheduleDB, "eventrecord");
							GridFSInputFile fsFile = fs.createFile(inputStream);
							fsFile.setFilename(fileName);
							fsFile.setContentType("audio/amr");
							fsFile.save();
							inputStream.close();
						}
						
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
