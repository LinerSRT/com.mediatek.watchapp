package com.nostra13.universalimageloader.cache.disc.impl;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.IoUtils.CopyListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseDiskCache implements DiskCache {
    public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    protected int bufferSize = 32768;
    protected final File cacheDir;
    protected CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality = 100;
    protected final FileNameGenerator fileNameGenerator;
    protected final File reserveCacheDir;

    public BaseDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir argument must be not null");
        } else if (fileNameGenerator != null) {
            this.cacheDir = cacheDir;
            this.reserveCacheDir = reserveCacheDir;
            this.fileNameGenerator = fileNameGenerator;
        } else {
            throw new IllegalArgumentException("fileNameGenerator argument must be not null");
        }
    }

    public File get(String imageUri) {
        return getFile(imageUri);
    }

    public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
        OutputStream os;
        File imageFile = getFile(imageUri);
        File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
        boolean loaded = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(tmpFile), this.bufferSize);
            loaded = IoUtils.copyStream(imageStream, os, listener, this.bufferSize);
            IoUtils.closeSilently(os);
            if (loaded && !tmpFile.renameTo(imageFile)) {
                loaded = false;
            }
            if (!loaded) {
                tmpFile.delete();
            }
            return loaded;
        } catch (Throwable th) {
            if (loaded && !tmpFile.renameTo(imageFile)) {
                loaded = false;
            }
            if (!loaded) {
                tmpFile.delete();
            }
        }
    }

    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        File imageFile = getFile(imageUri);
        File tmpFile = new File(imageFile.getAbsolutePath() + ".tmp");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), this.bufferSize);
        try {
            boolean savedSuccessfully = bitmap.compress(this.compressFormat, this.compressQuality, os);
            IoUtils.closeSilently(os);
            if (savedSuccessfully && !tmpFile.renameTo(imageFile)) {
                savedSuccessfully = false;
            }
            if (!savedSuccessfully) {
                tmpFile.delete();
            }
            bitmap.recycle();
            return savedSuccessfully;
        } catch (Throwable th) {
            IoUtils.closeSilently(os);
            if (null == null) {
                tmpFile.delete();
            }
        }
    }

    protected File getFile(String imageUri) {
        String fileName = this.fileNameGenerator.generate(imageUri);
        File dir = this.cacheDir;
        if (!(this.cacheDir.exists() || this.cacheDir.mkdirs() || this.reserveCacheDir == null)) {
            if (this.reserveCacheDir.exists() || this.reserveCacheDir.mkdirs()) {
                dir = this.reserveCacheDir;
            }
        }
        return new File(dir, fileName);
    }
}
