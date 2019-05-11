package com.nostra13.universalimageloader.core;

import android.graphics.Bitmap;
import android.os.Handler;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.FailReason.FailType;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.utils.C0197L;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.IoUtils.CopyListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

final class LoadAndDisplayImageTask implements Runnable, CopyListener {
    private final ImageLoaderConfiguration configuration;
    private final ImageDecoder decoder;
    private final ImageDownloader downloader;
    private final ImageLoaderEngine engine;
    private final Handler handler;
    final ImageAware imageAware;
    private final ImageLoadingInfo imageLoadingInfo;
    final ImageLoadingListener listener;
    private LoadedFrom loadedFrom = LoadedFrom.NETWORK;
    private final String memoryCacheKey;
    private final ImageDownloader networkDeniedDownloader;
    final DisplayImageOptions options;
    final ImageLoadingProgressListener progressListener;
    private final ImageDownloader slowNetworkDownloader;
    private final boolean syncLoading;
    private final ImageSize targetSize;
    final String uri;

    /* renamed from: com.nostra13.universalimageloader.core.LoadAndDisplayImageTask$3 */
    class C01923 implements Runnable {
        C01923() {
        }

        public void run() {
            LoadAndDisplayImageTask.this.listener.onLoadingCancelled(LoadAndDisplayImageTask.this.uri, LoadAndDisplayImageTask.this.imageAware.getWrappedView());
        }
    }

    class TaskCancelledException extends Exception {
        TaskCancelledException() {
        }
    }

    public LoadAndDisplayImageTask(ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler) {
        this.engine = engine;
        this.imageLoadingInfo = imageLoadingInfo;
        this.handler = handler;
        this.configuration = engine.configuration;
        this.downloader = this.configuration.downloader;
        this.networkDeniedDownloader = this.configuration.networkDeniedDownloader;
        this.slowNetworkDownloader = this.configuration.slowNetworkDownloader;
        this.decoder = this.configuration.decoder;
        this.uri = imageLoadingInfo.uri;
        this.memoryCacheKey = imageLoadingInfo.memoryCacheKey;
        this.imageAware = imageLoadingInfo.imageAware;
        this.targetSize = imageLoadingInfo.targetSize;
        this.options = imageLoadingInfo.options;
        this.listener = imageLoadingInfo.listener;
        this.progressListener = imageLoadingInfo.progressListener;
        this.syncLoading = this.options.isSyncLoading();
    }

    public void run() {
        if (!waitIfPaused() && !delayIfNeed()) {
            ReentrantLock loadFromUriLock = this.imageLoadingInfo.loadFromUriLock;
            C0197L.m0d("Start display image task [%s]", this.memoryCacheKey);
            if (loadFromUriLock.isLocked()) {
                C0197L.m0d("Image already is loading. Waiting... [%s]", this.memoryCacheKey);
            }
            loadFromUriLock.lock();
            try {
                checkTaskNotActual();
                Bitmap bmp = this.configuration.memoryCache.get(this.memoryCacheKey);
                if (bmp != null) {
                    if (!bmp.isRecycled()) {
                        this.loadedFrom = LoadedFrom.MEMORY_CACHE;
                        C0197L.m0d("...Get cached bitmap from memory after waiting. [%s]", this.memoryCacheKey);
                        if (bmp != null && this.options.shouldPostProcess()) {
                            C0197L.m0d("PostProcess image before displaying [%s]", this.memoryCacheKey);
                            bmp = this.options.getPostProcessor().process(bmp);
                            if (bmp == null) {
                                C0197L.m1e("Post-processor returned null [%s]", this.memoryCacheKey);
                            }
                        }
                        checkTaskNotActual();
                        checkTaskInterrupted();
                        runTask(new DisplayBitmapTask(bmp, this.imageLoadingInfo, this.engine, this.loadedFrom), this.syncLoading, this.handler, this.engine);
                        return;
                    }
                }
                bmp = tryLoadBitmap();
                if (bmp != null) {
                    checkTaskNotActual();
                    checkTaskInterrupted();
                    if (this.options.shouldPreProcess()) {
                        C0197L.m0d("PreProcess image before caching in memory [%s]", this.memoryCacheKey);
                        bmp = this.options.getPreProcessor().process(bmp);
                        if (bmp == null) {
                            C0197L.m1e("Pre-processor returned null [%s]", this.memoryCacheKey);
                        }
                    }
                    if (bmp != null) {
                        if (this.options.isCacheInMemory()) {
                            C0197L.m0d("Cache image in memory [%s]", this.memoryCacheKey);
                            this.configuration.memoryCache.put(this.memoryCacheKey, bmp);
                        }
                    }
                    C0197L.m0d("PostProcess image before displaying [%s]", this.memoryCacheKey);
                    bmp = this.options.getPostProcessor().process(bmp);
                    if (bmp == null) {
                        C0197L.m1e("Post-processor returned null [%s]", this.memoryCacheKey);
                    }
                    checkTaskNotActual();
                    checkTaskInterrupted();
                    runTask(new DisplayBitmapTask(bmp, this.imageLoadingInfo, this.engine, this.loadedFrom), this.syncLoading, this.handler, this.engine);
                    return;
                }
                loadFromUriLock.unlock();
            } catch (TaskCancelledException e) {
                fireCancelEvent();
            } finally {
                loadFromUriLock.unlock();
            }
        }
    }

    private boolean waitIfPaused() {
        AtomicBoolean pause = this.engine.getPause();
        if (pause.get()) {
            synchronized (this.engine.getPauseLock()) {
                if (pause.get()) {
                    C0197L.m0d("ImageLoader is paused. Waiting...  [%s]", this.memoryCacheKey);
                    try {
                        this.engine.getPauseLock().wait();
                        C0197L.m0d(".. Resume loading [%s]", this.memoryCacheKey);
                    } catch (InterruptedException e) {
                        C0197L.m1e("Task was interrupted [%s]", this.memoryCacheKey);
                        return true;
                    }
                }
            }
        }
        return isTaskNotActual();
    }

    private boolean delayIfNeed() {
        if (!this.options.shouldDelayBeforeLoading()) {
            return false;
        }
        C0197L.m0d("Delay %d ms before loading...  [%s]", Integer.valueOf(this.options.getDelayBeforeLoading()), this.memoryCacheKey);
        try {
            Thread.sleep((long) this.options.getDelayBeforeLoading());
            return isTaskNotActual();
        } catch (InterruptedException e) {
            C0197L.m1e("Task was interrupted [%s]", this.memoryCacheKey);
            return true;
        }
    }

    private Bitmap tryLoadBitmap() throws TaskCancelledException {
        Object obj = 1;
        Bitmap bitmap = null;
        try {
            File imageFile = this.configuration.diskCache.get(this.uri);
            if (imageFile != null && imageFile.exists()) {
                if (imageFile.length() > 0) {
                    obj = null;
                }
                if (obj == null) {
                    C0197L.m0d("Load image from disk cache [%s]", this.memoryCacheKey);
                    this.loadedFrom = LoadedFrom.DISC_CACHE;
                    checkTaskNotActual();
                    bitmap = decodeImage(Scheme.FILE.wrap(imageFile.getAbsolutePath()));
                }
            }
            if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                return bitmap;
            }
            C0197L.m0d("Load image from network [%s]", this.memoryCacheKey);
            this.loadedFrom = LoadedFrom.NETWORK;
            String imageUriForDecoding = this.uri;
            if (this.options.isCacheOnDisk() && tryCacheImageOnDisk()) {
                imageFile = this.configuration.diskCache.get(this.uri);
                if (imageFile != null) {
                    imageUriForDecoding = Scheme.FILE.wrap(imageFile.getAbsolutePath());
                }
            }
            checkTaskNotActual();
            bitmap = decodeImage(imageUriForDecoding);
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                fireFailEvent(FailType.DECODING_ERROR, null);
            }
            return bitmap;
        } catch (IllegalStateException e) {
            fireFailEvent(FailType.NETWORK_DENIED, null);
        } catch (TaskCancelledException e2) {
            throw e2;
        } catch (IOException e3) {
            C0197L.m2e(e3);
            fireFailEvent(FailType.IO_ERROR, e3);
        } catch (OutOfMemoryError e4) {
            C0197L.m2e(e4);
            fireFailEvent(FailType.OUT_OF_MEMORY, e4);
        } catch (Throwable e5) {
            C0197L.m2e(e5);
            fireFailEvent(FailType.UNKNOWN, e5);
        }
    }

    private Bitmap decodeImage(String imageUri) throws IOException {
        String str = imageUri;
        return this.decoder.decode(new ImageDecodingInfo(this.memoryCacheKey, str, this.uri, this.targetSize, this.imageAware.getScaleType(), getDownloader(), this.options));
    }

    private boolean tryCacheImageOnDisk() throws TaskCancelledException {
        C0197L.m0d("Cache image on disk [%s]", this.memoryCacheKey);
        try {
            boolean loaded = downloadImage();
            if (!loaded) {
                return loaded;
            }
            int width = this.configuration.maxImageWidthForDiskCache;
            int height = this.configuration.maxImageHeightForDiskCache;
            if (width <= 0 && height <= 0) {
                return loaded;
            }
            C0197L.m0d("Resize image in disk cache [%s]", this.memoryCacheKey);
            resizeAndSaveImage(width, height);
            return loaded;
        } catch (IOException e) {
            C0197L.m2e(e);
            return false;
        }
    }

    private boolean downloadImage() throws IOException {
        InputStream is = getDownloader().getStream(this.uri, this.options.getExtraForDownloader());
        if (is != null) {
            try {
                boolean save = this.configuration.diskCache.save(this.uri, is, this);
                return save;
            } finally {
                IoUtils.closeSilently(is);
            }
        } else {
            C0197L.m1e("No stream for image [%s]", this.memoryCacheKey);
            return false;
        }
    }

    private boolean resizeAndSaveImage(int maxWidth, int maxHeight) throws IOException {
        File targetFile = this.configuration.diskCache.get(this.uri);
        if (targetFile == null || !targetFile.exists()) {
            return false;
        }
        Bitmap bmp = this.decoder.decode(new ImageDecodingInfo(this.memoryCacheKey, Scheme.FILE.wrap(targetFile.getAbsolutePath()), this.uri, new ImageSize(maxWidth, maxHeight), ViewScaleType.FIT_INSIDE, getDownloader(), new Builder().cloneFrom(this.options).imageScaleType(ImageScaleType.IN_SAMPLE_INT).build()));
        if (!(bmp == null || this.configuration.processorForDiskCache == null)) {
            C0197L.m0d("Process image before cache on disk [%s]", this.memoryCacheKey);
            bmp = this.configuration.processorForDiskCache.process(bmp);
            if (bmp == null) {
                C0197L.m1e("Bitmap processor for disk cache returned null [%s]", this.memoryCacheKey);
            }
        }
        if (bmp == null) {
            return false;
        }
        boolean saved = this.configuration.diskCache.save(this.uri, bmp);
        bmp.recycle();
        return saved;
    }

    public boolean onBytesCopied(int current, int total) {
        return this.syncLoading || fireProgressEvent(current, total);
    }

    private boolean fireProgressEvent(final int current, final int total) {
        if (isTaskInterrupted() || isTaskNotActual()) {
            return false;
        }
        if (this.progressListener != null) {
            runTask(new Runnable() {
                public void run() {
                    LoadAndDisplayImageTask.this.progressListener.onProgressUpdate(LoadAndDisplayImageTask.this.uri, LoadAndDisplayImageTask.this.imageAware.getWrappedView(), current, total);
                }
            }, false, this.handler, this.engine);
        }
        return true;
    }

    private void fireFailEvent(final FailType failType, final Throwable failCause) {
        if (!this.syncLoading && !isTaskInterrupted() && !isTaskNotActual()) {
            runTask(new Runnable() {
                public void run() {
                    if (LoadAndDisplayImageTask.this.options.shouldShowImageOnFail()) {
                        LoadAndDisplayImageTask.this.imageAware.setImageDrawable(LoadAndDisplayImageTask.this.options.getImageOnFail(LoadAndDisplayImageTask.this.configuration.resources));
                    }
                    LoadAndDisplayImageTask.this.listener.onLoadingFailed(LoadAndDisplayImageTask.this.uri, LoadAndDisplayImageTask.this.imageAware.getWrappedView(), new FailReason(failType, failCause));
                }
            }, false, this.handler, this.engine);
        }
    }

    private void fireCancelEvent() {
        if (!this.syncLoading && !isTaskInterrupted()) {
            runTask(new C01923(), false, this.handler, this.engine);
        }
    }

    private ImageDownloader getDownloader() {
        if (this.engine.isNetworkDenied()) {
            return this.networkDeniedDownloader;
        }
        if (this.engine.isSlowNetwork()) {
            return this.slowNetworkDownloader;
        }
        return this.downloader;
    }

    private void checkTaskNotActual() throws TaskCancelledException {
        checkViewCollected();
        checkViewReused();
    }

    private boolean isTaskNotActual() {
        return isViewCollected() || isViewReused();
    }

    private void checkViewCollected() throws TaskCancelledException {
        if (isViewCollected()) {
            throw new TaskCancelledException();
        }
    }

    private boolean isViewCollected() {
        if (!this.imageAware.isCollected()) {
            return false;
        }
        C0197L.m0d("ImageAware was collected by GC. Task is cancelled. [%s]", this.memoryCacheKey);
        return true;
    }

    private void checkViewReused() throws TaskCancelledException {
        if (isViewReused()) {
            throw new TaskCancelledException();
        }
    }

    private boolean isViewReused() {
        boolean imageAwareWasReused;
        if (this.memoryCacheKey.equals(this.engine.getLoadingUriForView(this.imageAware))) {
            imageAwareWasReused = false;
        } else {
            imageAwareWasReused = true;
        }
        if (!imageAwareWasReused) {
            return false;
        }
        C0197L.m0d("ImageAware is reused for another image. Task is cancelled. [%s]", this.memoryCacheKey);
        return true;
    }

    private void checkTaskInterrupted() throws TaskCancelledException {
        if (isTaskInterrupted()) {
            throw new TaskCancelledException();
        }
    }

    private boolean isTaskInterrupted() {
        if (!Thread.interrupted()) {
            return false;
        }
        C0197L.m0d("Task was interrupted [%s]", this.memoryCacheKey);
        return true;
    }

    String getLoadingUri() {
        return this.uri;
    }

    static void runTask(Runnable r, boolean sync, Handler handler, ImageLoaderEngine engine) {
        if (sync) {
            r.run();
        } else if (handler != null) {
            handler.post(r);
        } else {
            engine.fireCallback(r);
        }
    }
}
