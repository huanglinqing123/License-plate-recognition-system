package cc.eguid.charsocr.core;

import java.util.Vector;

import org.bytedeco.javacpp.opencv_core.Mat;
import cc.eguid.charsocr.core.CoreFunc.Color;


public class CharsRecognise {

    public void loadANN(final String s) {
        charsIdentify.loadModel(s);
    }

  
    public String charsRecognise(final Mat plate) {

       
        Vector<Mat> matVec = new Vector<Mat>();
       
        String plateIdentify = "";
        int result = charsSegment.charsSegment(plate, matVec);
       
        if (0 == result) {
            for (int j = 0; j < matVec.size(); j++) {
                Mat charMat = matVec.get(j);
               
                String charcater = charsIdentify.charsIdentify(charMat, (0 == j), (1 == j));
                plateIdentify = plateIdentify + charcater;
            }
        }

        return plateIdentify;
    }

    public void setCRDebug(final boolean isDebug) {
        charsSegment.setDebug(isDebug);
    }

  
    public boolean getCRDebug() {
        return charsSegment.getDebug();
    }


    public final String getPlateType(final Mat input) {
        String color = "Î´Öª";
        Color result = CoreFunc.getPlateType(input, true);
        if (Color.BLUE == result)
            color = "À¶ÅÆ";
        if (Color.YELLOW == result)
            color = "»ÆÅÆ";
        return color;
    }

 
    public void setLiuDingSize(final int param) {
        charsSegment.setLiuDingSize(param);
    }

  
    public void setColorThreshold(final int param) {
        charsSegment.setColorThreshold(param);
    }

  
    public void setBluePercent(final float param) {
        charsSegment.setBluePercent(param);
    }

   
    public final float getBluePercent() {
        return charsSegment.getBluePercent();
    }

    public void setWhitePercent(final float param) {
        charsSegment.setWhitePercent(param);
    }

  
    public final float getWhitePercent() {
        return charsSegment.getWhitePercent();
    }

    private CharsSegment charsSegment = new CharsSegment();

    private CharsIdentify charsIdentify = new CharsIdentify();
}
