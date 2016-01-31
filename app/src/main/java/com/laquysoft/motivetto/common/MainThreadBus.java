package com.laquysoft.motivetto.common;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

/**
 * Created by joaobiriba on 30/01/16.
 */
@Singleton
public class MainThreadBus extends Bus {
    private static final String LOG_TAG = MainThreadBus.class.getSimpleName();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static MainThreadBus sInstance = new MainThreadBus();

    private MainThreadBus () {
    }

    public static MainThreadBus getInstance() {
        return sInstance;
    }
    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MainThreadBus.super.post(event);
                }
            });
        }
    }


}
