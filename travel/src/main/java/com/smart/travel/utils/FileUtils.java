package com.smart.travel.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yfan10x on 2015/9/10.
 */
public class FileUtils {

    public static void writeFile(Context context, String filename, byte[] content) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
        outputStream.write(content);
        outputStream.close();
    }

    public static String readFile(Context context, String filename) throws IOException {
        StringBuffer stringBuffer = new StringBuffer(1024 * 8);
        FileInputStream in = context.openFileInput(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while ((line = br.readLine()) != null) {
            stringBuffer.append(line + "\n");
        }
        br.close();

        return stringBuffer.toString();
    }

    public static boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }

}
