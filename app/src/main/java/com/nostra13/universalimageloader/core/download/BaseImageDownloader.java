package com.nostra13.universalimageloader.core.download;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build.VERSION;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore.Video.Thumbnails;
import android.webkit.MimeTypeMap;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.utils.IoUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaseImageDownloader implements ImageDownloader {
    protected final int connectTimeout;
    protected final Context context;
    protected final int readTimeout;

    /* renamed from: com.nostra13.universalimageloader.core.download.BaseImageDownloader$1 */
    static /* synthetic */ class C01951 {
        /* renamed from: $SwitchMap$com$nostra13$universalimageloader$core$download$ImageDownloader$Scheme */
        static final /* synthetic */ int[] f6x4730d1a3 = new int[Scheme.values().length];

        static {
            try {
                f6x4730d1a3[Scheme.HTTP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f6x4730d1a3[Scheme.HTTPS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                f6x4730d1a3[Scheme.FILE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                f6x4730d1a3[Scheme.CONTENT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                f6x4730d1a3[Scheme.ASSETS.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                f6x4730d1a3[Scheme.DRAWABLE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                f6x4730d1a3[Scheme.UNKNOWN.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public BaseImageDownloader(Context context) {
        this(context, 5000, 20000);
    }

    public BaseImageDownloader(Context context, int connectTimeout, int readTimeout) {
        this.context = context.getApplicationContext();
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public InputStream getStream(String imageUri, Object extra) throws IOException {
        switch (C01951.f6x4730d1a3[Scheme.ofUri(imageUri).ordinal()]) {
            case 1:
            case 2:
                return getStreamFromNetwork(imageUri, extra);
            case 3:
                return getStreamFromFile(imageUri, extra);
            case 4:
                return getStreamFromContent(imageUri, extra);
            case 5:
                return getStreamFromAssets(imageUri, extra);
            case 6:
                return getStreamFromDrawable(imageUri, extra);
            default:
                return getStreamFromOtherSource(imageUri, extra);
        }
    }

    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpURLConnection conn = createConnection(imageUri, extra);
        int redirectCount = 0;
        while (conn.getResponseCode() / 100 == 3 && redirectCount < 5) {
            conn = createConnection(conn.getHeaderField("Location"), extra);
            redirectCount++;
        }
        try {
            InputStream imageStream = conn.getInputStream();
            if (shouldBeProcessed(conn)) {
                return new ContentLengthInputStream(new BufferedInputStream(imageStream, 32768), conn.getContentLength());
            }
            IoUtils.closeSilently(imageStream);
            throw new IOException("Image request failed with response code " + conn.getResponseCode());
        } catch (IOException e) {
            IoUtils.readAndCloseStream(conn.getErrorStream());
            throw e;
        }
    }

    protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
        return conn.getResponseCode() == 200;
    }

    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(Uri.encode(url, "@#&=*+-_.,:!?()/~'%")).openConnection();
        conn.setConnectTimeout(this.connectTimeout);
        conn.setReadTimeout(this.readTimeout);
        return conn;
    }

    protected InputStream getStreamFromFile(String imageUri, Object extra) throws IOException {
        String filePath = Scheme.FILE.crop(imageUri);
        if (isVideoFileUri(imageUri)) {
            return getVideoThumbnailStream(filePath);
        }
        return new ContentLengthInputStream(new BufferedInputStream(new FileInputStream(filePath), 32768), (int) new File(filePath).length());
    }

    @TargetApi(8)
    private InputStream getVideoThumbnailStream(String filePath) {
        if (VERSION.SDK_INT >= 8) {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, 2);
            if (bitmap != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.PNG, 0, bos);
                return new ByteArrayInputStream(bos.toByteArray());
            }
        }
        return null;
    }

    protected InputStream getStreamFromContent(String imageUri, Object extra) throws FileNotFoundException {
        ContentResolver res = this.context.getContentResolver();
        Uri uri = Uri.parse(imageUri);
        if (isVideoContentUri(uri)) {
            Bitmap bitmap = Thumbnails.getThumbnail(res, Long.valueOf(uri.getLastPathSegment()).longValue(), 1, null);
            if (bitmap != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.PNG, 0, bos);
                return new ByteArrayInputStream(bos.toByteArray());
            }
        } else if (imageUri.startsWith("content://com.android.contacts/")) {
            return getContactPhotoStream(uri);
        }
        return res.openInputStream(uri);
    }

    @TargetApi(14)
    protected InputStream getContactPhotoStream(Uri uri) {
        ContentResolver res = this.context.getContentResolver();
        if (VERSION.SDK_INT < 14) {
            return Contacts.openContactPhotoInputStream(res, uri);
        }
        return Contacts.openContactPhotoInputStream(res, uri, true);
    }

    protected InputStream getStreamFromAssets(String imageUri, Object extra) throws IOException {
        return this.context.getAssets().open(Scheme.ASSETS.crop(imageUri));
    }

    protected InputStream getStreamFromDrawable(String imageUri, Object extra) {
        return this.context.getResources().openRawResource(Integer.parseInt(Scheme.DRAWABLE.crop(imageUri)));
    }

    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        throw new UnsupportedOperationException(String.format("UIL doesn't support scheme(protocol) by default [%s]. You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))", new Object[]{imageUri}));
    }

    private boolean isVideoContentUri(Uri uri) {
        String mimeType = this.context.getContentResolver().getType(uri);
        if (mimeType != null && mimeType.startsWith("video/")) {
            return true;
        }
        return false;
    }

    private boolean isVideoFileUri(String uri) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri));
        if (mimeType != null && mimeType.startsWith("video/")) {
            return true;
        }
        return false;
    }
}
