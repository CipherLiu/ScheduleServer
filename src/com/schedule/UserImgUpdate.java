package com.schedule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Servlet implementation class UserImgUpdate
 */
public class UserImgUpdate extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserImgUpdate() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		  //��ʱĿ¼
		  String temp=request.getSession().getServletContext().getRealPath("/")+"temp";   
		  //�ϴ��ļ����Ŀ¼
		  String loadpath=request.getSession().getServletContext().getRealPath("/")+"Image"; 
          DiskFileUpload fu =new DiskFileUpload();
          fu.setSizeMax(1*1024*1024);   // ���������û��ϴ��ļ���С,��λ:�ֽ�
          fu.setSizeThreshold(4096);   // �������ֻ�������ڴ��д洢������,��λ:�ֽ�
          fu.setRepositoryPath(temp); // ����һ���ļ���С����getSizeThreshold()��ֵʱ���ݴ����Ӳ�̵�Ŀ¼
         
          //��ʼ��ȡ�ϴ���Ϣ
          int index=0;
          List fileItems =null;
          try {
        	  fileItems = fu.parseRequest(request);
              }catch(Exception e){
            	  e.printStackTrace();
              }
          Iterator iter = fileItems.iterator(); // ���δ���ÿ���ϴ����ļ�
          while (iter.hasNext())
          {
              FileItem item = (FileItem)iter.next();// �������������ļ�������б���Ϣ
              if (!item.isFormField())
              {
                  String name = item.getName();//��ȡ�ϴ��ļ���,����·��
                  name=name.substring(name.lastIndexOf("\\")+1);//��ȫ·������ȡ�ļ���
                  long size = item.getSize();
                  if((name==null||name.equals("")) && size==0)
                        continue;
                  int point = name.indexOf(".");
                  name=(new Date()).getTime()+name.substring(point,name.length())+index;
                  index++;
                  File fNew=new File(loadpath, name);
                  try{
                	  item.write(fNew);
                	  
                     } catch (Exception e){
                           e.printStackTrace();
                     }
              }
              else//ȡ�������ļ�������б���Ϣ
              {
            	 
           //�����������ӦдΪ��(תΪUTF-8����)
                  //String fieldvalue = new String(item.getString().getBytes(),"UTF-8");
              }
          }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
