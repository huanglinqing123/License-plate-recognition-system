package org.easypr.test;

import java.text.ParseException;  
import java.text.SimpleDateFormat;  
import java.util.Calendar;  
import java.util.Date;  
  
public class DateTest 
{  
  
    public static void main(String[] args) throws ParseException 
    {  

        Date d = new Date();  
        System.out.println(d);  
      
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
        String dateNowStr = sdf.format(d);  
        System.out.println("格式化后的日期：" + dateNowStr);  
          

    }  
}  