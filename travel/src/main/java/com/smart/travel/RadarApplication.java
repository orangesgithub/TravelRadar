package com.smart.travel;

import android.app.Application;

import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

/**
 * Created by yfan10x on 2015/10/28.
 */
public class RadarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading)
                .showImageOnFail(R.drawable.loading)
                .cacheInMemory(true)
                .cacheOnDisk(true).build();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this)
                .threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCache(new UsingFreqLimitedMemoryCache(20 * 1024 * 1024))
                .diskCacheFileCount(200)
                .defaultDisplayImageOptions(options)
                .imageDownloader(new BaseImageDownloader(this, 5 * 1000, 30 * 1000)).build();
        ImageLoader.getInstance().init(configuration);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ImageLoader.getInstance().destroy();
    }
}
