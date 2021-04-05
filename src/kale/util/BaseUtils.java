package kale.util;

import java.io.File;

/**
 * @author: ygtao
 * @date: 2020/11/1 19:27
 */
public class BaseUtils {

    public static String initBaseLogPath() {
        //项目根路径
        String path = System.getProperty("user.dir");
        String logPath = path + File.separator + "log";
        String basePath = "";
        File file = new File(logPath);
        if (!file.exists()) {
            //创建文件夹
            basePath = logPath + File.separator + "0";
        } else {
            int count = 0;
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    count++;
                }
            }
            basePath = logPath + File.separator + count;

        }
        File file1 = new File(basePath);
        file1.mkdirs();
        return basePath;
    }

    public static void main(String[] args) {
        System.out.println(initBaseLogPath());
    }
}
