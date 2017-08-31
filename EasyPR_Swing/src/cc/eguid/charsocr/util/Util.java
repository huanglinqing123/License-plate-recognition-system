package cc.eguid.charsocr.util;

import java.io.File;
import java.util.Vector;



public class Util {

    
    public static void getFiles(final String path, Vector<String> files) {
        getFiles(new File(path), files);
    }

    
    public static void recreateDir(final String dir) {
        new File(dir).delete();
        new File(dir).mkdir();
    }
    
    private static void getFiles(final File dir, Vector<String> files) {
        File[] filelist = dir.listFiles();
        for (File file : filelist) {
            if (file.isDirectory()) {
                getFiles(file, files);
            } else {
                files.add(file.getAbsolutePath());
            }
        }
    }
}
