package com.nostra13.universalimageloader.cache.disc.impl.ext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.Editor;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.utils.C0197L;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.IoUtils.CopyListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LruDiskCache implements DiskCache {
    public static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    protected int bufferSize = 32768;
    protected DiskLruCache cache;
    protected CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
    protected int compressQuality = 100;
    protected final FileNameGenerator fileNameGenerator;
    private File reserveCacheDir;

    public java.io.File get(java.lang.String r6) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0021 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/1519736165.run(Unknown Source)
*/
        /*
        r5 = this;
        r2 = 0;
        r1 = 0;
        r3 = r5.cache;	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
        r4 = r5.getKey(r6);	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
        r1 = r3.get(r4);	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
        if (r1 == 0) goto L_0x0013;	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
    L_0x000e:
        r3 = 0;	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
        r2 = r1.getFile(r3);	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
    L_0x0013:
        if (r1 != 0) goto L_0x0016;
    L_0x0015:
        return r2;
    L_0x0016:
        r1.close();
        goto L_0x0015;
    L_0x001a:
        r0 = move-exception;
        com.nostra13.universalimageloader.utils.C0197L.m2e(r0);	 Catch:{ IOException -> 0x001a, all -> 0x0025 }
        if (r1 != 0) goto L_0x0021;
    L_0x0020:
        return r2;
    L_0x0021:
        r1.close();
        goto L_0x0020;
    L_0x0025:
        r2 = move-exception;
        if (r1 != 0) goto L_0x0029;
    L_0x0028:
        throw r2;
    L_0x0029:
        r1.close();
        goto L_0x0028;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache.get(java.lang.String):java.io.File");
    }

    public LruDiskCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator, long cacheMaxSize, int cacheMaxFileCount) throws IOException {
        Object obj = null;
        if (cacheDir != null) {
            if (cacheMaxSize >= 0) {
                obj = 1;
            }
            if (obj == null) {
                throw new IllegalArgumentException("cacheMaxSize argument must be positive number");
            } else if (cacheMaxFileCount < 0) {
                throw new IllegalArgumentException("cacheMaxFileCount argument must be positive number");
            } else if (fileNameGenerator != null) {
                if (cacheMaxSize == 0) {
                    cacheMaxSize = Long.MAX_VALUE;
                }
                if (cacheMaxFileCount == 0) {
                    cacheMaxFileCount = Integer.MAX_VALUE;
                }
                this.reserveCacheDir = reserveCacheDir;
                this.fileNameGenerator = fileNameGenerator;
                initCache(cacheDir, reserveCacheDir, cacheMaxSize, cacheMaxFileCount);
                return;
            } else {
                throw new IllegalArgumentException("fileNameGenerator argument must be not null");
            }
        }
        throw new IllegalArgumentException("cacheDir argument must be not null");
    }

    private void initCache(File cacheDir, File reserveCacheDir, long cacheMaxSize, int cacheMaxFileCount) throws IOException {
        try {
            this.cache = DiskLruCache.open(cacheDir, 1, 1, cacheMaxSize, cacheMaxFileCount);
        } catch (IOException e) {
            C0197L.m2e(e);
            if (reserveCacheDir != null) {
                initCache(reserveCacheDir, null, cacheMaxSize, cacheMaxFileCount);
            }
            if (this.cache == null) {
                throw e;
            }
        }
    }

    public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
        Editor editor = this.cache.edit(getKey(imageUri));
        if (editor == null) {
            return false;
        }
        OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), this.bufferSize);
        try {
            boolean copied = IoUtils.copyStream(imageStream, os, listener, this.bufferSize);
            IoUtils.closeSilently(os);
            if (copied) {
                editor.commit();
            } else {
                editor.abort();
            }
            return copied;
        } catch (Throwable th) {
            IoUtils.closeSilently(os);
            editor.abort();
        }
    }

    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        Editor editor = this.cache.edit(getKey(imageUri));
        if (editor == null) {
            return false;
        }
        OutputStream os = new BufferedOutputStream(editor.newOutputStream(0), this.bufferSize);
        boolean z = false;
        try {
            z = bitmap.compress(this.compressFormat, this.compressQuality, os);
            if (z) {
                editor.commit();
            } else {
                editor.abort();
            }
            return z;
        } finally {
            IoUtils.closeSilently(os);
        }
    }

    private String getKey(String imageUri) {
        return this.fileNameGenerator.generate(imageUri);
    }
}
