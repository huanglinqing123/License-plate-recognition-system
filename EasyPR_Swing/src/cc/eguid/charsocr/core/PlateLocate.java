package cc.eguid.charsocr.core;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

import java.util.Vector;

import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.RotatedRect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;


public class PlateLocate {

    
    public void setLifemode(boolean islifemode) {
        if (islifemode) {
            setGaussianBlurSize(5);
            setMorphSizeWidth(9);
            setMorphSizeHeight(3);
            setVerifyError(0.9f);
            setVerifyAspect(4);
            setVerifyMin(1);
            setVerifyMax(30);
        } else {
            setGaussianBlurSize(DEFAULT_GAUSSIANBLUR_SIZE);
            setMorphSizeWidth(DEFAULT_MORPH_SIZE_WIDTH);
            setMorphSizeHeight(DEFAULT_MORPH_SIZE_HEIGHT);
            setVerifyError(DEFAULT_ERROR);
            setVerifyAspect(DEFAULT_ASPECT);
            setVerifyMin(DEFAULT_VERIFY_MIN);
            setVerifyMax(DEFAULT_VERIFY_MAX);
        }
    }

    
    public Vector<Mat> plateLocate(Mat src) {
        Vector<Mat> resultVec = new Vector<Mat>();

        Mat src_blur = new Mat();
        Mat src_gray = new Mat();
        Mat grad = new Mat();

        int scale = SOBEL_SCALE;
        int delta = SOBEL_DELTA;
        int ddepth = SOBEL_DDEPTH;

      
        GaussianBlur(src, src_blur, new Size(gaussianBlurSize, gaussianBlurSize), 0, 0, BORDER_DEFAULT);
        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_GaussianBlur.jpg", src_blur);
        }

      
        cvtColor(src_blur, src_gray, CV_RGB2GRAY);
        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_gray.jpg", src_gray);
        }

       
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        Sobel(src_gray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_x, abs_grad_x);

        Sobel(src_gray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
        convertScaleAbs(grad_y, abs_grad_y);

        
        addWeighted(abs_grad_x, SOBEL_X_WEIGHT, abs_grad_y, SOBEL_Y_WEIGHT, 0, grad);

        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_Sobel.jpg", grad);
        }

       

        Mat img_threshold = new Mat();
        threshold(grad, img_threshold, 0, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);

        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_threshold.jpg", img_threshold);
        }

       

        Mat element = getStructuringElement(MORPH_RECT, new Size(morphSizeWidth, morphSizeHeight));
        morphologyEx(img_threshold, img_threshold, MORPH_CLOSE, element);

        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_morphology.jpg", img_threshold);
        }

       

        MatVector contours = new MatVector();
        findContours(img_threshold, contours, 
                CV_RETR_EXTERNAL, 
                CV_CHAIN_APPROX_NONE); 

        Mat result = new Mat();
        if (debug) {
            
            src.copyTo(result);
            drawContours(result, contours, -1, new Scalar(0, 0, 255, 255));
            opencv_imgcodecs.imwrite("tmp/debug_Contours.jpg", result);
        }

       
        Vector<RotatedRect> rects = new Vector<RotatedRect>();

        for (int i = 0; i < contours.size(); ++i) {
            RotatedRect mr = minAreaRect(contours.get(i));
            if (verifySizes(mr))
                rects.add(mr);
        }

        int k = 1;
        for (int i = 0; i < rects.size(); i++) {
            RotatedRect minRect = rects.get(i);
            if (verifySizes(minRect)) {

                if (debug) {
                    Point2f rect_points = new Point2f(4);
                    minRect.points(rect_points);

                    for (int j = 0; j < 4; j++) {
                    	
                        Point pt1 = new Point(new CvPoint2D32f(rect_points.position(j)));
                       
                        Point pt2 = new Point(new CvPoint2D32f(rect_points.position((j + 1) % 4)));
                    	
                        line(result, pt1, pt2, new Scalar(0, 255, 255, 255), 1, 8, 0);
                    }
                }

                float r = minRect.size().width() / minRect.size().height();
                float angle = minRect.angle();
                Size rect_size = new Size((int) minRect.size().width(), (int) minRect.size().height());
                if (r < 1) {
                    angle = 90 + angle;
                    rect_size = new Size(rect_size.height(), rect_size.width());
                }
                if (angle - this.angle < 0 && angle + this.angle > 0) {
                    Mat rotmat = getRotationMatrix2D(minRect.center(), angle, 1);
                    Mat img_rotated = new Mat();
                    warpAffine(src, img_rotated, rotmat, src.size()); 
                    
                    Mat resultMat = showResultMat(img_rotated, rect_size, minRect.center(), k++);
                    resultVec.add(resultMat);
                }
            }
        }
        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_result.jpg", result);
        }

        return resultVec;
    }

   

    public void setGaussianBlurSize(int gaussianBlurSize) {
        this.gaussianBlurSize = gaussianBlurSize;
    }

    public final int getGaussianBlurSize() {
        return this.gaussianBlurSize;
    }

    public void setMorphSizeWidth(int morphSizeWidth) {
        this.morphSizeWidth = morphSizeWidth;
    }

    public final int getMorphSizeWidth() {
        return this.morphSizeWidth;
    }

    public void setMorphSizeHeight(int morphSizeHeight) {
        this.morphSizeHeight = morphSizeHeight;
    }

    public final int getMorphSizeHeight() {
        return this.morphSizeHeight;
    }

    public void setVerifyError(float error) {
        this.error = error;
    }

    public final float getVerifyError() {
        return this.error;
    }

    public void setVerifyAspect(float aspect) {
        this.aspect = aspect;
    }

    public final float getVerifyAspect() {
        return this.aspect;
    }

    public void setVerifyMin(int verifyMin) {
        this.verifyMin = verifyMin;
    }

    public void setVerifyMax(int verifyMax) {
        this.verifyMax = verifyMax;
    }

    public void setJudgeAngle(int angle) {
        this.angle = angle;
    }

    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    
    public boolean getDebug() {
        return debug;
    }
    
    
    private boolean verifySizes(RotatedRect mr) {
        float error = this.error;

       
        float aspect = this.aspect;
        int min = 44 * 14 * verifyMin; 
        int max = 44 * 14 * verifyMax;
        
        
        float rmin = aspect - aspect * error;
        float rmax = aspect + aspect * error;

        int area = (int) (mr.size().height() * mr.size().width());
        float r = mr.size().width() / mr.size().height();
        if (r < 1)
            r = mr.size().height() / mr.size().width();
        
        return area >= min && area <= max && r >= rmin && r <= rmax;
    }

    
    private Mat showResultMat(Mat src, Size rect_size, Point2f center, int index) {
        Mat img_crop = new Mat();
        getRectSubPix(src, rect_size, center, img_crop);

        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_crop_" + index + ".jpg", img_crop);
        }

        Mat resultResized = new Mat();
        resultResized.create(HEIGHT, WIDTH, TYPE);
        resize(img_crop, resultResized, resultResized.size(), 0, 0, INTER_CUBIC);
        if (debug) {
        	opencv_imgcodecs.imwrite("tmp/debug_resize_" + index + ".jpg", resultResized);
        }
        return resultResized;
    }

    public static final int DEFAULT_GAUSSIANBLUR_SIZE = 5;
    public static final int SOBEL_SCALE = 1;
    public static final int SOBEL_DELTA = 0;
    public static final int SOBEL_DDEPTH = CV_16S;
    public static final int SOBEL_X_WEIGHT = 1;
    public static final int SOBEL_Y_WEIGHT = 0;
    public static final int DEFAULT_MORPH_SIZE_WIDTH = 17;
    public static final int DEFAULT_MORPH_SIZE_HEIGHT = 3;

    public static final int WIDTH = 136;
    public static final int HEIGHT = 36;
    public static final int TYPE = CV_8UC3;


    public static final int DEFAULT_VERIFY_MIN = 3;
    public static final int DEFAULT_VERIFY_MAX = 20;

    final float DEFAULT_ERROR = 0.6f;
    final float DEFAULT_ASPECT = 3.75f;

    public static final int DEFAULT_ANGLE = 30;


    public static final boolean DEFAULT_DEBUG = true;

 
    protected int gaussianBlurSize = DEFAULT_GAUSSIANBLUR_SIZE;

   
    protected int morphSizeWidth = DEFAULT_MORPH_SIZE_WIDTH;
    protected int morphSizeHeight = DEFAULT_MORPH_SIZE_HEIGHT;

   
    protected float error = DEFAULT_ERROR;
    protected float aspect = DEFAULT_ASPECT;
    protected int verifyMin = DEFAULT_VERIFY_MIN;
    protected int verifyMax = DEFAULT_VERIFY_MAX;


    protected int angle = DEFAULT_ANGLE;

    protected boolean debug = DEFAULT_DEBUG;
}
