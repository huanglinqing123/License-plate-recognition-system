package cc.eguid.charsocr.core;

import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.countNonZero;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGB2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_OTSU;
import static org.bytedeco.javacpp.opencv_imgproc.INTER_LINEAR;
import static org.bytedeco.javacpp.opencv_imgproc.boundingRect;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.findContours;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;
import static org.bytedeco.javacpp.opencv_imgproc.warpAffine;
import static cc.eguid.charsocr.core.CoreFunc.getPlateType;

import java.util.Vector;

import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import cc.eguid.charsocr.util.Convert;


public class CharsSegment {

    
    public int charsSegment(final Mat input, Vector<Mat> resultVec) {
        if (input.data().isNull())
            return -3;

       
       

        Mat img_threshold = new Mat();

        Mat input_grey = new Mat();
        cvtColor(input, input_grey, CV_RGB2GRAY);

        int w = input.cols();
        int h = input.rows();
        Mat tmpMat = new Mat(input, new Rect((int) (w * 0.1), (int) (h * 0.1), (int) (w * 0.8), (int) (h * 0.8)));
       
        switch (getPlateType(tmpMat, true)) {
        case BLUE:
            threshold(input_grey, img_threshold, 10, 255, CV_THRESH_OTSU + CV_THRESH_BINARY);
            break;
        case YELLOW:
            threshold(input_grey, img_threshold, 10, 255, CV_THRESH_OTSU + CV_THRESH_BINARY_INV);
            break;
        default:
            return -3;
        }
       
        if (this.isDebug) {
        	opencv_imgcodecs.imwrite("tmp/debug_char_threshold.jpg", img_threshold);
        }
      
        if (this.isDebug) {
            String str = "tmp/debug_char_clearLiuDing.jpg";
            opencv_imgcodecs.imwrite(str, img_threshold);
        }

      
        Mat img_contours = new Mat();
        img_threshold.copyTo(img_contours);

        MatVector contours = new MatVector();

        findContours(img_contours, contours, 
                CV_RETR_EXTERNAL, 
                CV_CHAIN_APPROX_NONE); 

      
        Vector<Rect> vecRect = new Vector<Rect>();
        for (int i = 0; i < contours.size(); ++i) {
            Rect mr = boundingRect(contours.get(i));
            if (verifySizes(new Mat(img_threshold, mr)))
                vecRect.add(mr);
        }

        if (vecRect.size() == 0)
            return -3;

        Vector<Rect> sortedRect = new Vector<Rect>();
      
        SortRect(vecRect, sortedRect);

      
        int specIndex = GetSpecificRect(sortedRect);

        if (this.isDebug) {
            if (specIndex < sortedRect.size()) {
                Mat specMat = new Mat(img_threshold, sortedRect.get(specIndex));
                String str = "tmp/debug_specMat.jpg";
                opencv_imgcodecs.imwrite(str, specMat);
            }
        }

     
        Rect chineseRect = new Rect();
        if (specIndex < sortedRect.size())
            chineseRect = GetChineseRect(sortedRect.get(specIndex));
        else
            return -3;

        if (this.isDebug) {
            Mat chineseMat = new Mat(img_threshold, chineseRect);
            String str = "tmp/debug_chineseMat.jpg";
            opencv_imgcodecs.imwrite(str, chineseMat);
        }

        Vector<Rect> newSortedRect = new Vector<Rect>();
        newSortedRect.add(chineseRect);
        RebuildRect(sortedRect, newSortedRect, specIndex);

        if (newSortedRect.size() == 0)
            return -3;

        for (int i = 0; i < newSortedRect.size(); i++) {
            Rect mr = newSortedRect.get(i);
            Mat auxRoi = new Mat(img_threshold, mr);

            auxRoi = preprocessChar(auxRoi);
            if (this.isDebug) {
                String str = "tmp/debug_char_auxRoi_" + Integer.valueOf(i).toString() + ".jpg";
                opencv_imgcodecs.imwrite(str, auxRoi);
            }
            resultVec.add(auxRoi);
        }
        return 0;
    }

    
    private Boolean verifySizes(Mat r) {
        float aspect = 45.0f / 90.0f;
        float charAspect = (float) r.cols() / (float) r.rows();
        float error = 0.7f;
        float minHeight = 10f;
        float maxHeight = 35f;
       
        float minAspect = 0.05f;
        float maxAspect = aspect + aspect * error;
       
        float area = countNonZero(r);
        
        float bbArea = r.cols() * r.rows();
       
        float percPixels = area / bbArea;

        return percPixels <= 1 && charAspect > minAspect && charAspect < maxAspect && r.rows() >= minHeight
                && r.rows() < maxHeight;
    }

    
    private Mat preprocessChar(Mat in) {
        int h = in.rows();
        int w = in.cols();
        int charSize = CHAR_SIZE;
        Mat transformMat = Mat.eye(2, 3, CV_32F).asMat();
        int m = Math.max(w, h);
        transformMat.ptr(0, 2).put(Convert.getBytes(((m - w) / 2f)));
        transformMat.ptr(1, 2).put(Convert.getBytes((m - h) / 2f));

        Mat warpImage = new Mat(m, m, in.type());
        
        warpAffine(in, warpImage, transformMat, warpImage.size(), INTER_LINEAR, opencv_core.BORDER_CONSTANT, new Scalar(0));

        Mat out = new Mat();
        resize(warpImage, out, new Size(charSize, charSize));

        return out;
    }

    
    private Mat clearLiuDing(Mat img) {
        final int x = this.liuDingSize;
       
        Mat jump = Mat.zeros(1, img.rows(), CV_32F).asMat();
    	CoreFunc.showImage("test", jump);
      
        System.err.println(img.rows());
        for (int i = 0; i < img.rows(); i++) {
            int jumpCount = 0;
            for (int j = 0; j < img.cols() - 1; j++) {
                if (img.ptr(i, j).get() != img.ptr(i, j + 1).get())
                    jumpCount++;
            }
            System.err.println(jumpCount);
          
            byte[] bt=Convert.getBytes((float) jumpCount);
            System.err.println("test:"+bt.length);
      
            BytePointer bp= jump.ptr(i);
            bp.put(bt);
            System.err.println("test:end");
        }
       
        for (int i = 0; i < img.rows(); i++) {
            if (Convert.toFloat(jump.ptr(i)) <= x) {
                for (int j = 0; j < img.cols(); j++) {
                    img.ptr(i, j).put((byte) 0);
                }
            }
        }
        return img;
    }

    
    private Rect GetChineseRect(final Rect rectSpe) {
        int height = rectSpe.height();
        float newwidth = rectSpe.width() * 1.15f;
        int x = rectSpe.x();
        int y = rectSpe.y();

        int newx = x - (int) (newwidth * 1.15);
        newx = Math.max(newx, 0);
        Rect a = new Rect(newx, y, (int) newwidth, height);
        return a;
    }

    
    private int GetSpecificRect(final Vector<Rect> vecRect) {
        Vector<Integer> xpositions = new Vector<Integer>();
        int maxHeight = 0;
        int maxWidth = 0;
        for (int i = 0; i < vecRect.size(); i++) {
            xpositions.add(vecRect.get(i).x());

            if (vecRect.get(i).height() > maxHeight) {
                maxHeight = vecRect.get(i).height();
            }
            if (vecRect.get(i).width() > maxWidth) {
                maxWidth = vecRect.get(i).width();
            }
        }

        int specIndex = 0;
        for (int i = 0; i < vecRect.size(); i++) {
            Rect mr = vecRect.get(i);
            int midx = mr.x() + mr.width() / 2;

            if ((mr.width() > maxWidth * 0.8 || mr.height() > maxHeight * 0.8)
                    && (midx < this.theMatWidth * 2 / 7 && midx > this.theMatWidth / 7)) {
                specIndex = i;
            }
        }

        return specIndex;
    }

    
    private int RebuildRect(final Vector<Rect> vecRect, Vector<Rect> outRect, int specIndex) {
         int count = 6;
        for (int i = 0; i < vecRect.size(); i++) {
               if (i < specIndex)
                continue;

            outRect.add(vecRect.get(i));
            if (--count == 0)
                break;
        }

        return 0;
    }

    
    private void SortRect(final Vector<Rect> vecRect, Vector<Rect> out) {
        Vector<Integer> orderIndex = new Vector<Integer>();
        Vector<Integer> xpositions = new Vector<Integer>();
        for (int i = 0; i < vecRect.size(); ++i) {
            orderIndex.add(i);
            xpositions.add(vecRect.get(i).x());
        }

        float min = xpositions.get(0);
        int minIdx;
        for (int i = 0; i < xpositions.size(); ++i) {
            min = xpositions.get(i);
            minIdx = i;
            for (int j = i; j < xpositions.size(); ++j) {
                if (xpositions.get(j) < min) {
                    min = xpositions.get(j);
                    minIdx = j;
                }
            }
            int aux_i = orderIndex.get(i);
            int aux_min = orderIndex.get(minIdx);
            orderIndex.remove(i);
            orderIndex.insertElementAt(aux_min, i);
            orderIndex.remove(minIdx);
            orderIndex.insertElementAt(aux_i, minIdx);

            float aux_xi = xpositions.get(i);
            float aux_xmin = xpositions.get(minIdx);
            xpositions.remove(i);
            xpositions.insertElementAt((int) aux_xmin, i);
            xpositions.remove(minIdx);
            xpositions.insertElementAt((int) aux_xi, minIdx);
        }

        for (int i = 0; i < orderIndex.size(); i++)
            out.add(vecRect.get(orderIndex.get(i)));

        return;
    }

    public void setLiuDingSize(int param) {
        this.liuDingSize = param;
    }

    public void setColorThreshold(int param) {
        this.colorThreshold = param;
    }

    public void setBluePercent(float param) {
        this.bluePercent = param;
    }

    public final float getBluePercent() {
        return this.bluePercent;
    }

    public void setWhitePercent(float param) {
        this.whitePercent = param;
    }

    public final float getWhitePercent() {
        return this.whitePercent;
    }

    public boolean getDebug() {
        return this.isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

     final static boolean DEFAULT_DEBUG = false;

     final static int CHAR_SIZE = 20;
    final static int HORIZONTAL = 1;
    final static int VERTICAL = 0;

    final static int DEFAULT_LIUDING_SIZE = 7;
    final static int DEFAULT_MAT_WIDTH = 136;

    final static int DEFAULT_COLORTHRESHOLD = 150;
    final static float DEFAULT_BLUEPERCEMT = 0.3f;
    final static float DEFAULT_WHITEPERCEMT = 0.1f;

    private int liuDingSize = DEFAULT_LIUDING_SIZE;
    private int theMatWidth = DEFAULT_MAT_WIDTH;

    private int colorThreshold = DEFAULT_COLORTHRESHOLD;
    private float bluePercent = DEFAULT_BLUEPERCEMT;
    private float whitePercent = DEFAULT_WHITEPERCEMT;

    private boolean isDebug = DEFAULT_DEBUG;
}
