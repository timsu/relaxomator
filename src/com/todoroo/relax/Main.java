package com.todoroo.relax;

import gsearch.Client;
import gsearch.Result;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends Activity {
    private List<Result> results;
    private int current = 0;

    private ImageButton previous, next;
    private ImageView image;
    private TextView text;
    private TextView loading;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Client client = new Client();
        results = client.searchImagesPlus("cat", 60).getResults();

        previous = (ImageButton) findViewById(R.id.prev);
        next = (ImageButton) findViewById(R.id.next);
        image = (ImageView) findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);
        loading = (TextView) findViewById(R.id.loading);
        loadResult(0);

        previous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                current--;
                loadResult(current);
            }
        });

        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                current++;
                loadResult(current);
            }
        });

    }

    private void loadResult(final int i) {
        previous.setEnabled(i > 0);
        next.setEnabled(i < results.size() - 1);
        text.setText(String.format("Showing %d of %d", i+1, results.size()));

        loading.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = fetch(results.get(i).getUnescapedUrl());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image.setImageBitmap(bitmap);
                            loading.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {
                    Log.e("error", "error",  e);
                }
            }
        }).start();
    }

    /**
     * Retrieve icon that corresponds with this coach. Has two layers of caching,
     * session-level caching and in-database coach caching.
     *
     * @category tested
     * @return
     */
    public Bitmap fetch(String urlString) throws IOException {
        URL url = new URL(urlString);

        InputStream is = null;
        Bitmap bitmap = null;
        try {
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            int contentLength = conn.getContentLength();

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
        } finally {
            if(is != null)
                is.close();
        }

        return bitmap;
    }
}