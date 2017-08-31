package org.easypr.test;




import java.sql.*;
public class connect {
	
	
	public Connection conn = null;
	ResultSet rs = null;
	Statement statement = null;
	

    String driver = "com.mysql.jdbc.Driver";

   
    String url = "jdbc:mysql://115.159.110.50:3306/cardnum";


    String user = "jiangyingying"; 


    String password = "ipv6";

	public connect()
	{
		try { 
        
         Class.forName(driver);

      
         conn = DriverManager.getConnection(url, user, password);
         
         if(!conn.isClosed())         	 
        	 System.out.println("connect Succeeded");

        }
        
        	catch(ClassNotFoundException e) 
        	{
        		System.out.println("Sorry,can`t find the Driver!"); 
        		e.printStackTrace();
        	} catch(Exception e) {
            e.printStackTrace();
           } 
        //
	}
        
       
    	
    	public void closeconn(){
    		try 
    		{
    			conn.close();
    		}catch (Exception e)
    		{
   			 e.printStackTrace();
   		 	}
    	}
} 
