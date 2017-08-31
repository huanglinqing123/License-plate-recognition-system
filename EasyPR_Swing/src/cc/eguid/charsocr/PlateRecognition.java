package cc.eguid.charsocr;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import java.math.BigDecimal;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvType;
import org.bytedeco.javacpp.opencv_core.CvTypeInfo;
import org.bytedeco.javacpp.opencv_core.Mat;

import cc.eguid.charsocr.core.CharsRecognise;
import cc.eguid.charsocr.core.PlateDetect;


public class PlateRecognition {
	 static PlateDetect plateDetect =null;
	 static CharsRecognise cr=null;
	 static{
		plateDetect=new PlateDetect();
		plateDetect.setPDLifemode(true);
		cr = new CharsRecognise();
	 }
	
	
	    public static String plateRecognise(Mat mat){
	         Vector<Mat> matVector = new Vector<Mat>(1);
	         if (0 == plateDetect.plateDetect(mat, matVector)) {
	        	 
	             if(matVector.size()>0){
	            	 Mat plate = matVector.get(0);
	            	 String plateType = cr.getPlateType(plate);
	            	 String plateIdentify = cr.charsRecognise(matVector.get(0));
		                
		             String license = plateType + ":" + plateIdentify;
		                
		                
	            	 return license;
	             }
	         }
	         return null;
	    }
	   
	    public static String[] mutiPlateRecognise(Mat mat){
	    	 PlateDetect plateDetect = new PlateDetect();
	         plateDetect.setPDLifemode(true);
	         Vector<Mat> matVector = new Vector<Mat>(10);
	         if (0 == plateDetect.plateDetect(mat, matVector)) {
	             CharsRecognise cr = new CharsRecognise();
	             String[] results=new String[matVector.size()];
	             for (int i = 0; i < matVector.size(); ++i) {
	                 String result = cr.charsRecognise(matVector.get(i));
	               results[i]=result;
	             }
	             return results;
	         }
	         return null;
	    }
	  
	    public static String plateRecognise(String imgPath){
	    	 Mat src = opencv_imgcodecs.imread(imgPath);
	    	 return plateRecognise(src);
	    }
	  
	    public static String[] mutiPlateRecognise(String imgPath){
	    	Mat src = opencv_imgcodecs.imread(imgPath);
	    	return mutiPlateRecognise(src);
	    }
	    
	    public static void main(String[] args){
	    	int sum=100;
	    	int errNum=0;
	    	int sumTime=0;
	    	long longTime=0;
	    	for(int i=sum;i>0;i--){
	    	 String imgPath = "res/image/test_image/4.jpg";
	    	 Mat src = opencv_imgcodecs.imread(imgPath);
	    	 long now =System.currentTimeMillis();
	    	String ret=plateRecognise(src);
	    	System.err.println(ret);

	    	}
	    
	    	}
}
