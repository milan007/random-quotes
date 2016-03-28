package com.mentatmobile.randomquotes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class GetImage extends AsyncTask<Object, Void, Bitmap>{

    private static final String LOG_TAG = GetImage.class.getName();
    private static final String FILE_NAME_TEMPLATE = "image%03d.png";

    private static final String SERVICE_URL = "https://services-mentatmobile.rhcloud.com/randomquotes/services/secure/image/";
    //private static final String SERVICE_URL = "http://192.168.1.94:8180/randomquotes/services/secure/image/";

    @Override
    protected Bitmap doInBackground(Object... params) {
        return downloadImage((Integer) params[0], (Context) params[1]);
    }

    private Bitmap downloadImage(Integer imageId, Context context) {
        Bitmap bitmap = null;

        try {
            String fileName = String.format(FILE_NAME_TEMPLATE, imageId);

            bitmap = readFile(fileName, context);
            if (bitmap != null) {
                Log.d(LOG_TAG, "Returned " + fileName + " from private store...");
                return bitmap;
            }

            URL url = new URL(SERVICE_URL + imageId);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(2000);
            conn.connect();

            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(bis);
            writeFile(fileName, bitmap, context);
            bis.close();
            is.close();

            Log.d(LOG_TAG, "Returned " + fileName + " from URL " + url.getPath() + "..." );
        }
        catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

        return bitmap;
    }

    private void writeFile(String fileName, Bitmap bitmap, Context context) throws Exception{
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
    }

    private Bitmap readFile(String fileName, Context context){
        Bitmap bitmap = null;
        try {
            FileInputStream input = context.openFileInput(fileName);
            bitmap = BitmapFactory.decodeStream(input);
        }
        catch(Exception e){
        }

        return bitmap;
    }
}
