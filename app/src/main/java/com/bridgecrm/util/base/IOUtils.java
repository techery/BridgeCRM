package com.bridgecrm.util.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class IOUtils {

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
          /*
           * To convert the InputStream to String we use the BufferedReader.readLine()
           * method. We iterate until the BufferedReader return null which means
           * there's no more data to read. Each line will appended to a StringBuilder
           * and returned as String.
           */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }


}
