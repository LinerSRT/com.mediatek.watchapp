package com.nostra13.universalimageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.C0197L;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

public class ImageLoader {
    public static final String TAG = ImageLoader.class.getSimpleName();
    private static volatile ImageLoader instance;
    private ImageLoaderConfiguration configuration;
    private ImageLoadingListener defaultListener = new SimpleImageLoadingListener();
    private ImageLoaderEngine engine;

    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader();
                }
            }
        }
        return instance;
    }

    protected ImageLoader() {
    }

    public synchronized void init(ImageLoaderConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("ImageLoader configuration can not be initialized with null");
        } else if (this.configuration != null) {
            C0197L.m4w("Try to initialize ImageLoader which had already been initialized before. To re-init ImageLoader with new configuration call ImageLoader.destroy() at first.", new Object[0]);
        } else {
            C0197L.m0d("Initialize ImageLoader with configuration", new Object[0]);
            this.engine = new ImageLoaderEngine(configuration);
            this.configuration = configuration;
        }
    }

    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        displayImage(uri, imageAware, options, null, listener, progressListener);
    }

    public void displayImage(String uri, ImageAware imageAware, DisplayImageOptions options, ImageSize targetSize, ImageLoadingListener listener, ImageLoadingProgressListener progressListener) {
        checkConfiguration();
        if (imageAware != null) {
            if (listener == null) {
                listener = this.defaultListener;
            }
            if (options == null) {
                options = this.configuration.defaultDisplayImageOptions;
            }
            if (TextUtils.isEmpty(uri)) {
                this.engine.cancelDisplayTaskFor(imageAware);
                listener.onLoadingStarted(uri, imageAware.getWrappedView());
                if (options.shouldShowImageForEmptyUri()) {
                    imageAware.setImageDrawable(options.getImageForEmptyUri(this.configuration.resources));
                } else {
                    imageAware.setImageDrawable(null);
                }
                listener.onLoadingComplete(uri, imageAware.getWrappedView(), null);
                return;
            }
            if (targetSize == null) {
                targetSize = ImageSizeUtils.defineTargetSizeForView(imageAware, this.configuration.getMaxImageSize());
            }
            String memoryCacheKey = MemoryCacheUtils.generateKey(uri, targetSize);
            this.engine.prepareDisplayTaskFor(imageAware, memoryCacheKey);
            listener.onLoadingStarted(uri, imageAware.getWrappedView());
            Bitmap bmp = this.configuration.memoryCache.get(memoryCacheKey);
            if (bmp == null || bmp.isRecycled()) {
                if (options.shouldShowImageOnLoading()) {
                    imageAware.setImageDrawable(options.getImageOnLoading(this.configuration.resources));
                } else if (options.isResetViewBeforeLoading()) {
                    imageAware.setImageDrawable(null);
                }
                LoadAndDisplayImageTask displayTask = new LoadAndDisplayImageTask(this.engine, new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey, options, listener, progressListener, this.engine.getLockForUri(uri)), defineHandler(options));
                if (options.isSyncLoading()) {
                    displayTask.run();
                } else {
                    this.engine.submit(displayTask);
                }
            } else {
                C0197L.m0d("Load image from memory cache [%s]", memoryCacheKey);
                if (options.shouldPostProcess()) {
                    ProcessAndDisplayImageTask displayTask2 = new ProcessAndDisplayImageTask(this.engine, bmp, new ImageLoadingInfo(uri, imageAware, targetSize, memoryCacheKey, options, listener, progressListener, this.engine.getLockForUri(uri)), defineHandler(options));
                    if (options.isSyncLoading()) {
                        displayTask2.run();
                    } else {
                        this.engine.submit(displayTask2);
                    }
                } else {
                    options.getDisplayer().display(bmp, imageAware, LoadedFrom.MEMORY_CACHE);
                    listener.onLoadingComplete(uri, imageAware.getWrappedView(), bmp);
                }
            }
            return;
        }
        throw new IllegalArgumentException("Wrong arguments were passed to displayImage() method (ImageView reference must not be null)");
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayImage(uri, new ImageViewAware(imageView), options, null, null);
    }

    private void checkConfiguration() {
        if (this.configuration == null) {
            throw new IllegalStateException("ImageLoader must be init with configuration before using");
        }
    }

    private static Handler defineHandler(DisplayImageOptions options) {
        Handler handler = options.getHandler();
        if (options.isSyncLoading()) {
            return null;
        }
        if (handler == null && Looper.myLooper() == Looper.getMainLooper()) {
            return new Handler();
        }
        return handler;
    }
}
