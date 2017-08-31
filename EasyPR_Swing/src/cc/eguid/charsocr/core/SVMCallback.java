package cc.eguid.charsocr.core;

import org.bytedeco.javacpp.opencv_core.Mat;



public interface SVMCallback {

    
    public abstract Mat getHisteqFeatures(final Mat image);

    
    public abstract Mat getHistogramFeatures(final Mat image);

    
    public abstract Mat getSIFTFeatures(final Mat image);


    public abstract Mat getHOGFeatures(final Mat image);
}
