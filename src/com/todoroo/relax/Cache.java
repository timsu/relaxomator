package com.todoroo.relax;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.todoroo.andlib.service.ContextManager;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.service.RestClient;

/**
 * Caches REST api calls
 */
@SuppressWarnings("nls")
public class Cache {

    public Cache() {
        DependencyInjectionService.getInstance().inject(this);
    }

    // --- image fetching

    /**
     * Retrieve icon that corresponds with this coach. Has two layers of caching,
     * session-level caching and in-database coach caching.
     *
     * @category tested
     * @return
     */
    public Bitmap get(String urlString) throws IOException {
        if(contains(urlString))
            return getFromCache(urlString);

        URL url = new URL(urlString);
        System.err.println("fetching " + urlString);

        InputStream is = null;
        Bitmap bitmap = null;
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            int contentLength = conn.getContentLength();
            if(contentLength < 400000) {
                // write bytes to output stream
                byte[] bytes = new byte[contentLength];
                int offset = 0;
                while(true) {
                    int byteCount = is.read(bytes, offset, contentLength - offset);
                    if(byteCount == -1)
                        break;
                    else
                        offset += byteCount;
                }

                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } else {
                bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.icon)).getBitmap();
            }
        } finally {
            if(is != null)
                is.close();
        }
        mCache.put(urlString, bitmap);
        return bitmap;
    }

    // ---


    /**
     * Deletes a cached file so next time it's loaded the method will hit the server
     * @param url
     */
    public void delete(String url) {
        Context ctx = ContextManager.getContext();
        String filename = URLEncoder.encode(url);

        if(configurationService.isDebug())
            Log.d("caching-service", "deleting file " + filename);
        ctx.deleteFile(filename);
    }

    /**
     * Deletes all cached files with the given substring
     * @param substring
     */
    public void deleteAll(String substring) {
        Context ctx = ContextManager.getContext();
        String encoded = URLEncoder.encode(substring);

        for(String file : ctx.getFilesDir().list()) {
            if(file.contains(encoded)) {
                ctx.deleteFile(file);
                if(configurationService.isDebug())
                    Log.d("caching-service", "deleting file " + file +
                            " which matches " + encoded);
            }
        }
    }

    // ######################################################################################################################
    // requestHTTPWithCached
    public String get(boolean refresh, RestClient restClient, String url) throws IOException {
        Context ctx = ContextManager.getContext();
        String filename = URLEncoder.encode(url);
        String response = null;

        response = readTextFile(ctx, filename);

        if ((response == null) || (refresh == true)) {
            if(configurationService.isDebug())
                Log.e("bente-rest-cache-miss", url); //$NON-NLS-1$
            try {
                response = restClient.get(url);
                writeTextFile(ctx, filename, response);
            } catch (IOException e) {
                throw e;
            }
        }
        return response;

    }

    // ######################################################################################################################
    // readTextFile
    public String readTextFile(Context ctx, String filename) throws IOException {
        try {
            FileInputStream fIn = ctx.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fIn);

            StringBuffer out = new StringBuffer();
            char[] b = new char[4096];
            for (int n; (n = isr.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }

            return out.toString();
        } catch (FileNotFoundException e) {
            // expected if we have no cache for this thing
            return null;
        }
    }

    // ######################################################################################################################
    // writeTextFile
    public void writeTextFile(Context context, String filename, String data) throws IOException {
        FileOutputStream fOut = null;
        OutputStreamWriter osw = null;
        try {
            fOut = context.openFileOutput(filename, Context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fOut);
            osw.write(data);
            osw.flush();
            return;
        } catch (IOException e) {
            // log an error, delete the file
            exceptionService.reportError("error caching", e); //$NON-NLS-1$
        } finally {
            try {
                if (osw != null)
                    osw.close();
                if (fOut != null)
                    fOut.close();
            } catch (IOException e) {
                exceptionService.reportError("error caching", e); //$NON-NLS-1$
            }
        }

        // if we got down here, delete the file
        context.deleteFile(filename);
    }

    // ######################################################################################################################
    // END
}