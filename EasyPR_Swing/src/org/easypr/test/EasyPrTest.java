package org.easypr.test;

import org.bytedeco.javacpp.opencv_core.Mat;
import cc.eguid.charsocr.PlateRecognition;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.bytedeco.javacpp.opencv_imgcodecs;

public class EasyPrTest {

	
	//
	public String ceshi(){
		String imgPath = "res/image/test_image/plate_judge1.jpg";
		Mat src = opencv_imgcodecs.imread(imgPath);
		PlateRecognition a = new PlateRecognition();
		String ret = a.plateRecognise(src);

		return ret;
		
	}

}
