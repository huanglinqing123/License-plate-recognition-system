package cc.eguid.charsocr.core;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static cc.eguid.charsocr.core.CoreFunc.features;

import java.util.HashMap;
import java.util.Map;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_ml.ANN_MLP;
import cc.eguid.charsocr.util.Convert;


public class CharsIdentify {

    public CharsIdentify() {
        loadModel();

        if (this.map.isEmpty()) {
        	 map.put("zh_cuan", "川");
             map.put("zh_e", "鄂");
             map.put("zh_gan", "赣");
             map.put("zh_gan1", "甘");
             map.put("zh_gui", "贵");
             map.put("zh_gui1", "桂");
             map.put("zh_hei", "黑");
             map.put("zh_hu", "沪");
             map.put("zh_ji", "冀");
             map.put("zh_jin", "津");
             map.put("zh_jing", "京");
             map.put("zh_jl", "吉");
             map.put("zh_liao", "辽");
             map.put("zh_lu", "鲁");
             map.put("zh_meng", "蒙");
             map.put("zh_min", "闽");
             map.put("zh_ning", "宁");
             map.put("zh_qing", "青");
             map.put("zh_qiong", "琼");
             map.put("zh_shan", "陕");
             map.put("zh_su", "苏");
             map.put("zh_sx", "晋");
             map.put("zh_wan", "皖");
             map.put("zh_xiang", "湘");
             map.put("zh_xin", "新");
             map.put("zh_yu", "豫");
             map.put("zh_yu1", "渝");
             map.put("zh_yue", "粤");
             map.put("zh_yun", "云");
             map.put("zh_zang", "藏");
             map.put("zh_zhe", "浙");
        }
    }

    
    public String charsIdentify(final Mat input, final Boolean isChinese, final Boolean isSpeci) {
        String result = "";
        Mat f = features(input, this.predictSize);
        int index = classify(f, isChinese, isSpeci);
        if (!isChinese) {
            result = String.valueOf(strCharacters[index]);
        } else {
            String s = strChinese[index - numCharacter];
            result = map.get(s);
        }
        return result;
    }

    
    private int classify( Mat f, final Boolean isChinses, final Boolean isSpeci) {
        int result = -1;
        Mat output = new Mat(1, numAll, CV_32FC1);
       
        ann.predict(f, output, 0);
        int ann_min = (!isChinses) ? ((isSpeci) ? 10 : 0) : numCharacter;
        int ann_max = (!isChinses) ? numCharacter : numAll;
        float maxVal = -2;
        for (int j = ann_min; j < ann_max; j++) {
            float val = Convert.toFloat(output.ptr(0, j));
            if (val > maxVal) {
                maxVal = val;
                result = j;
            }
        }
        return result;
    }

    private void loadModel() {
        loadModel(this.path);
    }

    public void loadModel(String s) {
       ann.clear();
       ann=ANN_MLP.loadANN_MLP(s, "ann");
    }

    static boolean hasPrint = false;

    public final void setModelPath(String path) {
        this.path = path;
    }

    public final String getModelPath() {
        return this.path;
    }

   
    private ANN_MLP ann=ANN_MLP.create();

    private String path = "res/model/ann.xml";

    private int predictSize = 10;

    private Map<String, String> map = new HashMap<String, String>();

    private final char strCharacters[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z' };
    private final static int numCharacter = 34; 

    private final String strChinese[] = { "zh_cuan" , "zh_e" , "zh_gan" , "zh_gan1",
            "zh_gui", "zh_gui1", "zh_hei" , "zh_hu" , "zh_ji" , "zh_jin" ,
            "zh_jing" , "zh_jl" , "zh_liao" , "zh_lu" , "zh_meng" ,
            "zh_min" , "zh_ning" , "zh_qing" , "zh_qiong" , "zh_shan" ,
            "zh_su" , "zh_sx" , "zh_wan" , "zh_xiang" , "zh_xin" , "zh_yu" ,
            "zh_yu1" , "zh_yue" , "zh_yun" , "zh_zang" , "zh_zhe" };
    @SuppressWarnings("unused")
    private final static int numChinese = 31;

    private final static int numAll = 65; 
}
