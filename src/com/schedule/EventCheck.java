package com.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Servlet implementation class EventCheck
 */
public class EventCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Mongo connection;
    private DB scheduleDB;
    private DBCollection eventCollection; 
    private Date dateToCheck = new Date();
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EventCheck() {
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
		String dateTimeInMillis = request.getParameter("dateTimeInMillis");
		dateToCheck.setTime(Long.parseLong(dateTimeInMillis));
		//String username = new String(request.getParameter("username").getBytes("ISO-8859-1"),"UTF-8"); 
		try{
			connection = new Mongo();
			scheduleDB = connection.getDB("schedule");
			eventCollection = scheduleDB.getCollection("event_" + userId);

			BasicDBObject eventCheckQuery = new BasicDBObject();
			BasicDBObject conditionOne = new BasicDBObject();
			BasicDBObject conditionTwo = new BasicDBObject();
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(dateToCheck.getTime());
			Date dateBegin = new Date();
			Date dateEnd = new Date();
			ArrayList<Boolean> hasEventArray = new ArrayList<Boolean>();
			for(int i = 0 ; i < 42 ; i++){
				dateBegin.setTime(cal.getTimeInMillis());
				conditionTwo.put("$gte", dateBegin);
				cal.roll(Calendar.DAY_OF_YEAR, true);
				dateEnd.setTime(cal.getTimeInMillis());
				conditionOne.put("$lte", dateEnd);
				
				eventCheckQuery.put("calFrom",conditionOne);
				eventCheckQuery.put("calTo",conditionTwo);
				DBCursor cur = eventCollection.find(eventCheckQuery);
				if(cur.hasNext()){
					hasEventArray.add(true);
				}else{
					hasEventArray.add(false);
				}
				
			}
			jb.put("result", Primitive.ACCEPT);
			jb.put("hasEventArray",hasEventArray);
			
		}catch(MongoException e){
			jb.put("result", Primitive.DBCONNECTIONERROR);
		   	e.printStackTrace();
		}
		PrintWriter writer = response.getWriter();
		writer.write(jb.toString());
		writer.flush();
		writer.close();
	}

	private String calendarToString(Calendar calendar){
		Date date = new Date(calendar.getTimeInMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		return dateFormat.format(date);
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
