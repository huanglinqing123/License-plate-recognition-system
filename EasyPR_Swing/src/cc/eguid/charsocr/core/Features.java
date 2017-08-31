package cc.eguid.charsocr.core;

import static org.bytedeco.javacpp.opencv_core.merge;
import static org.bytedeco.javacpp.opencv_core.split;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static cc.eguid.charsocr.core.CoreFunc.features;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;


public class Features implements SVMCallback {

    
    @Override
    public Mat getHisteqFeatures(final Mat image) {
        return histeq(image);
    }

    
    @Override
    public Mat getHistogramFeatures(Mat image) {
        Mat grayImage = new Mat();
        cvtColor(image, grayImage, CV_RGB2GRAY);

        Mat img_threshold = new Mat();
        threshold(grayImage, img_threshold, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);

        return features(img_threshold, 0);
    }

    
    @Override
    public Mat getSIFTFeatures(final Mat image) {
        
        return null;
    }

    
    @Override
    public Mat getHOGFeatures(final Mat image) {
       return null;
    }

    private Mat histeq(Mat in) {
        Mat out = new Mat(in.size(), in.type());
        if (in.channels() == 3) {
            Mat hsv = new Mat();
            MatVector hsvSplit = new MatVector();
            cvtColor(in, hsv, CV_BGR2HSV);
            split(hsv, hsvSplit);
            equalizeHist(hsvSplit.get(2), hsvSplit.get(2));
            merge(hsvSplit, hsv);
            cvtColor(hsv, out, CV_HSV2BGR);
            hsv = null;
            hsvSplit = null;
            System.gc();
        } else if (in.channels() == 1) {
            equalizeHist(in, out);
        }
        return out;
    }
}
