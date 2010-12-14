package com.todoroo.relax;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.todoroo.andlib.service.ContextManager;
import com.todoroo.andlib.service.DependencyInjectionService;
import com.todoroo.andlib.utility.Preferences;

public class Main extends Activity {
    private int current, total;

    static {
        RelaxDependencyInjector.initialize();
    }

    private Gallery gallery;
    private ImageView image;
    private TextView loading;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        ContextManager.setContext(this);
        DependencyInjectionService.getInstance().inject(this);

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        current = Preferences.getInt("current", 0);
        total = Preferences.getInt("total", 0);
    }

    // --- results loading

    private Thread loadingThread = null;

    private void loadResult() {
        Preferences.setInt("total", total);
        Preferences.setInt("current", current);

        previous.setEnabled(total + current > 0);
        next.setEnabled(imageSource.hasMoreResults(total + current));
        text.setText(String.format("%d", total + current + 1));

        loading.setVisibility(View.VISIBLE);

        if(loadingThread != null)
            loadingThread.interrupt();
        loadingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = fetch(results[current]);

                    if(Thread.interrupted())
                        return;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image.setImageBitmap(bitmap);
                            loading.setVisibility(View.GONE);
                        }
                    });

                } catch (Throwable e) {
                    Log.e("error", "error",  e);
                }
            }
        });
        loadingThread.start();
    }

}