package com.nostra13.universalimageloader.cache.disc.impl.ext;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

final class DiskLruCache implements Closeable {
    static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");
    private static final OutputStream NULL_OUTPUT_STREAM = new C01852();
    private final int appVersion;
    private final Callable<Void> cleanupCallable = new C01841();
    private final File directory;
    final ThreadPoolExecutor executorService = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue());
    private int fileCount = 0;
    private final File journalFile;
    private final File journalFileBackup;
    private final File journalFileTmp;
    private Writer journalWriter;
    private final LinkedHashMap<String, Entry> lruEntries = new LinkedHashMap(0, 0.75f, true);
    private int maxFileCount;
    private long maxSize;
    private long nextSequenceNumber = 0;
    private int redundantOpCount;
    private long size = 0;
    private final int valueCount;

    /* renamed from: com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache$1 */
    class C01841 implements Callable<Void> {
        C01841() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.Void call() throws java.lang.Exception {
            /*
            r4 = this;
            r3 = 0;
            r1 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;
            monitor-enter(r1);
            r0 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;	 Catch:{ all -> 0x002e }
            r0 = r0.journalWriter;	 Catch:{ all -> 0x002e }
            if (r0 == 0) goto L_0x0020;
        L_0x000c:
            r0 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;	 Catch:{ all -> 0x002e }
            r0.trimToSize();	 Catch:{ all -> 0x002e }
            r0 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;	 Catch:{ all -> 0x002e }
            r0.trimToFileCount();	 Catch:{ all -> 0x002e }
            r0 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;	 Catch:{ all -> 0x002e }
            r0 = r0.journalRebuildRequired();	 Catch:{ all -> 0x002e }
            if (r0 != 0) goto L_0x0022;
        L_0x001e:
            monitor-exit(r1);	 Catch:{ all -> 0x002e }
            return r3;
        L_0x0020:
            monitor-exit(r1);	 Catch:{ all -> 0x002e }
            return r3;
        L_0x0022:
            r0 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;	 Catch:{ all -> 0x002e }
            r0.rebuildJournal();	 Catch:{ all -> 0x002e }
            r0 = com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.this;	 Catch:{ all -> 0x002e }
            r2 = 0;
            r0.redundantOpCount = r2;	 Catch:{ all -> 0x002e }
            goto L_0x001e;
        L_0x002e:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x002e }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.1.call():java.lang.Void");
        }
    }

    /* renamed from: com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache$2 */
    static class C01852 extends OutputStream {
        C01852() {
        }

        public void write(int b) throws IOException {
        }
    }

    public final class Editor {
        private boolean committed;
        private final Entry entry;
        private boolean hasErrors;
        private final boolean[] written;

        private class FaultHidingOutputStream extends FilterOutputStream {
            private FaultHidingOutputStream(OutputStream out) {
                super(out);
            }

            public void write(int oneByte) {
                try {
                    this.out.write(oneByte);
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }

            public void write(byte[] buffer, int offset, int length) {
                try {
                    this.out.write(buffer, offset, length);
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }

            public void close() {
                try {
                    this.out.close();
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }

            public void flush() {
                try {
                    this.out.flush();
                } catch (IOException e) {
                    Editor.this.hasErrors = true;
                }
            }
        }

        private Editor(Entry entry) {
            boolean[] zArr;
            this.entry = entry;
            if (entry.readable) {
                zArr = null;
            } else {
                zArr = new boolean[DiskLruCache.this.valueCount];
            }
            this.written = zArr;
        }

        public OutputStream newOutputStream(int index) throws IOException {
            OutputStream faultHidingOutputStream;
            synchronized (DiskLruCache.this) {
                if (this.entry.currentEditor == this) {
                    FileOutputStream outputStream;
                    if (!this.entry.readable) {
                        this.written[index] = true;
                    }
                    File dirtyFile = this.entry.getDirtyFile(index);
                    try {
                        outputStream = new FileOutputStream(dirtyFile);
                    } catch (FileNotFoundException e) {
                        DiskLruCache.this.directory.mkdirs();
                        try {
                            outputStream = new FileOutputStream(dirtyFile);
                        } catch (FileNotFoundException e2) {
                            return DiskLruCache.NULL_OUTPUT_STREAM;
                        }
                    }
                    faultHidingOutputStream = new FaultHidingOutputStream(outputStream);
                } else {
                    throw new IllegalStateException();
                }
            }
            return faultHidingOutputStream;
        }

        public void commit() throws IOException {
            if (this.hasErrors) {
                DiskLruCache.this.completeEdit(this, false);
                DiskLruCache.this.remove(this.entry.key);
            } else {
                DiskLruCache.this.completeEdit(this, true);
            }
            this.committed = true;
        }

        public void abort() throws IOException {
            DiskLruCache.this.completeEdit(this, false);
        }
    }

    private final class Entry {
        private Editor currentEditor;
        private final String key;
        private final long[] lengths;
        private boolean readable;
        private long sequenceNumber;

        private Entry(String key) {
            this.key = key;
            this.lengths = new long[DiskLruCache.this.valueCount];
        }

        public String getLengths() throws IOException {
            StringBuilder result = new StringBuilder();
            for (long size : this.lengths) {
                result.append(' ').append(size);
            }
            return result.toString();
        }

        private void setLengths(String[] strings) throws IOException {
            if (strings.length == DiskLruCache.this.valueCount) {
                int i = 0;
                while (i < strings.length) {
                    try {
                        this.lengths[i] = Long.parseLong(strings[i]);
                        i++;
                    } catch (NumberFormatException e) {
                        throw invalidLengths(strings);
                    }
                }
                return;
            }
            throw invalidLengths(strings);
        }

        private IOException invalidLengths(String[] strings) throws IOException {
            throw new IOException("unexpected journal line: " + Arrays.toString(strings));
        }

        public File getCleanFile(int i) {
            return new File(DiskLruCache.this.directory, this.key + "" + i);
        }

        public File getDirtyFile(int i) {
            return new File(DiskLruCache.this.directory, this.key + "" + i + ".tmp");
        }
    }

    public final class Snapshot implements Closeable {
        private File[] files;
        private final InputStream[] ins;
        private final String key;
        private final long[] lengths;
        private final long sequenceNumber;

        private Snapshot(String key, long sequenceNumber, File[] files, InputStream[] ins, long[] lengths) {
            this.key = key;
            this.sequenceNumber = sequenceNumber;
            this.files = files;
            this.ins = ins;
            this.lengths = lengths;
        }

        public File getFile(int index) {
            return this.files[index];
        }

        public void close() {
            for (InputStream in : this.ins) {
                Util.closeQuietly(in);
            }
        }
    }

    private DiskLruCache(File directory, int appVersion, int valueCount, long maxSize, int maxFileCount) {
        this.directory = directory;
        this.appVersion = appVersion;
        this.journalFile = new File(directory, "journal");
        this.journalFileTmp = new File(directory, "journal.tmp");
        this.journalFileBackup = new File(directory, "journal.bkp");
        this.valueCount = valueCount;
        this.maxSize = maxSize;
        this.maxFileCount = maxFileCount;
    }

    public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize, int maxFileCount) throws IOException {
        boolean z = true;
        if (maxSize <= 0) {
            z = false;
        }
        if (!z) {
            throw new IllegalArgumentException("maxSize <= 0");
        } else if (maxFileCount <= 0) {
            throw new IllegalArgumentException("maxFileCount <= 0");
        } else if (valueCount > 0) {
            File backupFile = new File(directory, "journal.bkp");
            if (backupFile.exists()) {
                File journalFile = new File(directory, "journal");
                if (journalFile.exists()) {
                    backupFile.delete();
                } else {
                    renameTo(backupFile, journalFile, false);
                }
            }
            DiskLruCache cache = new DiskLruCache(directory, appVersion, valueCount, maxSize, maxFileCount);
            if (cache.journalFile.exists()) {
                try {
                    cache.readJournal();
                    cache.processJournal();
                    cache.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache.journalFile, true), Util.US_ASCII));
                    return cache;
                } catch (IOException journalIsCorrupt) {
                    System.out.println("DiskLruCache " + directory + " is corrupt: " + journalIsCorrupt.getMessage() + ", removing");
                    cache.delete();
                }
            }
            directory.mkdirs();
            cache = new DiskLruCache(directory, appVersion, valueCount, maxSize, maxFileCount);
            cache.rebuildJournal();
            return cache;
        } else {
            throw new IllegalArgumentException("valueCount <= 0");
        }
    }

    private void readJournal() throws IOException {
        int lineCount;
        StrictLineReader reader = new StrictLineReader(new FileInputStream(this.journalFile), Util.US_ASCII);
        try {
            String magic = reader.readLine();
            String version = reader.readLine();
            String appVersionString = reader.readLine();
            String valueCountString = reader.readLine();
            String blank = reader.readLine();
            if ("libcore.io.DiskLruCache".equals(magic)) {
                if ("1".equals(version) && Integer.toString(this.appVersion).equals(appVersionString) && Integer.toString(this.valueCount).equals(valueCountString) && "".equals(blank)) {
                    lineCount = 0;
                    while (true) {
                        readJournalLine(reader.readLine());
                        lineCount++;
                    }
                }
            }
            throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
        } catch (EOFException e) {
            this.redundantOpCount = lineCount - this.lruEntries.size();
            Util.closeQuietly(reader);
        } catch (Throwable th) {
            Util.closeQuietly(reader);
        }
    }

    private void readJournalLine(String line) throws IOException {
        int firstSpace = line.indexOf(32);
        if (firstSpace != -1) {
            String key;
            int keyBegin = firstSpace + 1;
            int secondSpace = line.indexOf(32, keyBegin);
            if (secondSpace != -1) {
                key = line.substring(keyBegin, secondSpace);
            } else {
                key = line.substring(keyBegin);
                if (firstSpace == "REMOVE".length() && line.startsWith("REMOVE")) {
                    this.lruEntries.remove(key);
                    return;
                }
            }
            Entry entry = (Entry) this.lruEntries.get(key);
            if (entry == null) {
                entry = new Entry(key);
                this.lruEntries.put(key, entry);
            }
            if (secondSpace != -1 && firstSpace == "CLEAN".length() && line.startsWith("CLEAN")) {
                String[] parts = line.substring(secondSpace + 1).split(" ");
                entry.readable = true;
                entry.currentEditor = null;
                entry.setLengths(parts);
            } else if (secondSpace == -1 && firstSpace == "DIRTY".length() && line.startsWith("DIRTY")) {
                entry.currentEditor = new Editor(entry);
            } else {
                if (secondSpace == -1 && firstSpace == "READ".length()) {
                    if (!line.startsWith("READ")) {
                    }
                }
                throw new IOException("unexpected journal line: " + line);
            }
            return;
        }
        throw new IOException("unexpected journal line: " + line);
    }

    private void processJournal() throws IOException {
        deleteIfExists(this.journalFileTmp);
        Iterator<Entry> i = this.lruEntries.values().iterator();
        while (i.hasNext()) {
            Entry entry = (Entry) i.next();
            int t;
            if (entry.currentEditor != null) {
                entry.currentEditor = null;
                for (t = 0; t < this.valueCount; t++) {
                    deleteIfExists(entry.getCleanFile(t));
                    deleteIfExists(entry.getDirtyFile(t));
                }
                i.remove();
            } else {
                for (t = 0; t < this.valueCount; t++) {
                    this.size += entry.lengths[t];
                    this.fileCount++;
                }
            }
        }
    }

    private synchronized void rebuildJournal() throws IOException {
        if (this.journalWriter != null) {
            this.journalWriter.close();
        }
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFileTmp), Util.US_ASCII));
        writer.write("libcore.io.DiskLruCache");
        writer.write("\n");
        writer.write("1");
        writer.write("\n");
        writer.write(Integer.toString(this.appVersion));
        writer.write("\n");
        writer.write(Integer.toString(this.valueCount));
        writer.write("\n");
        writer.write("\n");
        for (Entry entry : this.lruEntries.values()) {
            if (entry.currentEditor == null) {
                writer.write("CLEAN " + entry.key + entry.getLengths() + '\n');
            } else {
                try {
                    writer.write("DIRTY " + entry.key + '\n');
                } finally {
                    writer.close();
                }
            }
        }
        if (this.journalFile.exists()) {
            renameTo(this.journalFile, this.journalFileBackup, true);
        }
        renameTo(this.journalFileTmp, this.journalFile, false);
        this.journalFileBackup.delete();
        this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), Util.US_ASCII));
    }

    private static void deleteIfExists(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException();
        }
    }

    private static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
        if (deleteDestination) {
            deleteIfExists(to);
        }
        if (!from.renameTo(to)) {
            throw new IOException();
        }
    }

    public synchronized Snapshot get(String key) throws IOException {
        int i;
        InputStream[] ins;
        checkNotClosed();
        validateKey(key);
        Entry entry = (Entry) this.lruEntries.get(key);
        if (entry == null) {
            return null;
        }
        if (!entry.readable) {
            return null;
        }
        File[] files = new File[this.valueCount];
        ins = new InputStream[this.valueCount];
        i = 0;
        while (i < this.valueCount) {
            try {
                File file = entry.getCleanFile(i);
                files[i] = file;
                ins[i] = new FileInputStream(file);
                i++;
            } catch (FileNotFoundException e) {
                for (i = 0; i < this.valueCount; i++) {
                    if (ins[i] == null) {
                        break;
                    }
                    Util.closeQuietly(ins[i]);
                }
                return null;
            }
        }
        this.redundantOpCount++;
        this.journalWriter.append("READ " + key + '\n');
        if (journalRebuildRequired()) {
            this.executorService.submit(this.cleanupCallable);
        }
        return new Snapshot(key, entry.sequenceNumber, files, ins, entry.lengths);
    }

    public Editor edit(String key) throws IOException {
        return edit(key, -1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.Editor edit(java.lang.String r7, long r8) throws java.io.IOException {
        /*
        r6 = this;
        r4 = 0;
        monitor-enter(r6);
        r6.checkNotClosed();	 Catch:{ all -> 0x0065 }
        r6.validateKey(r7);	 Catch:{ all -> 0x0065 }
        r2 = r6.lruEntries;	 Catch:{ all -> 0x0065 }
        r1 = r2.get(r7);	 Catch:{ all -> 0x0065 }
        r1 = (com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.Entry) r1;	 Catch:{ all -> 0x0065 }
        r2 = -1;
        r2 = (r8 > r2 ? 1 : (r8 == r2 ? 0 : -1));
        if (r2 == 0) goto L_0x0022;
    L_0x0016:
        if (r1 != 0) goto L_0x001a;
    L_0x0018:
        monitor-exit(r6);
        return r4;
    L_0x001a:
        r2 = r1.sequenceNumber;	 Catch:{ all -> 0x0065 }
        r2 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1));
        if (r2 != 0) goto L_0x0018;
    L_0x0022:
        if (r1 == 0) goto L_0x0059;
    L_0x0024:
        r2 = r1.currentEditor;	 Catch:{ all -> 0x0065 }
        if (r2 != 0) goto L_0x0068;
    L_0x002a:
        r0 = new com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache$Editor;	 Catch:{ all -> 0x0065 }
        r2 = 0;
        r0.<init>(r1);	 Catch:{ all -> 0x0065 }
        r1.currentEditor = r0;	 Catch:{ all -> 0x0065 }
        r2 = r6.journalWriter;	 Catch:{ all -> 0x0065 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0065 }
        r3.<init>();	 Catch:{ all -> 0x0065 }
        r4 = "DIRTY ";
        r3 = r3.append(r4);	 Catch:{ all -> 0x0065 }
        r3 = r3.append(r7);	 Catch:{ all -> 0x0065 }
        r4 = 10;
        r3 = r3.append(r4);	 Catch:{ all -> 0x0065 }
        r3 = r3.toString();	 Catch:{ all -> 0x0065 }
        r2.write(r3);	 Catch:{ all -> 0x0065 }
        r2 = r6.journalWriter;	 Catch:{ all -> 0x0065 }
        r2.flush();	 Catch:{ all -> 0x0065 }
        monitor-exit(r6);
        return r0;
    L_0x0059:
        r1 = new com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache$Entry;	 Catch:{ all -> 0x0065 }
        r2 = 0;
        r1.<init>(r7);	 Catch:{ all -> 0x0065 }
        r2 = r6.lruEntries;	 Catch:{ all -> 0x0065 }
        r2.put(r7, r1);	 Catch:{ all -> 0x0065 }
        goto L_0x002a;
    L_0x0065:
        r2 = move-exception;
        monitor-exit(r6);
        throw r2;
    L_0x0068:
        monitor-exit(r6);
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.edit(java.lang.String, long):com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache$Editor");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void completeEdit(com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.Editor r13, boolean r14) throws java.io.IOException {
        /*
        r12 = this;
        monitor-enter(r12);
        r2 = r13.entry;	 Catch:{ all -> 0x0074 }
        r8 = r2.currentEditor;	 Catch:{ all -> 0x0074 }
        if (r8 != r13) goto L_0x006e;
    L_0x000b:
        if (r14 != 0) goto L_0x0077;
    L_0x000d:
        r3 = 0;
    L_0x000e:
        r8 = r12.valueCount;	 Catch:{ all -> 0x0074 }
        if (r3 < r8) goto L_0x00b9;
    L_0x0012:
        r8 = r12.redundantOpCount;	 Catch:{ all -> 0x0074 }
        r8 = r8 + 1;
        r12.redundantOpCount = r8;	 Catch:{ all -> 0x0074 }
        r8 = 0;
        r2.currentEditor = r8;	 Catch:{ all -> 0x0074 }
        r8 = r2.readable;	 Catch:{ all -> 0x0074 }
        r8 = r8 | r14;
        if (r8 != 0) goto L_0x00f0;
    L_0x0023:
        r8 = r12.lruEntries;	 Catch:{ all -> 0x0074 }
        r9 = r2.key;	 Catch:{ all -> 0x0074 }
        r8.remove(r9);	 Catch:{ all -> 0x0074 }
        r8 = r12.journalWriter;	 Catch:{ all -> 0x0074 }
        r9 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0074 }
        r9.<init>();	 Catch:{ all -> 0x0074 }
        r10 = "REMOVE ";
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r10 = r2.key;	 Catch:{ all -> 0x0074 }
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r10 = 10;
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r9 = r9.toString();	 Catch:{ all -> 0x0074 }
        r8.write(r9);	 Catch:{ all -> 0x0074 }
    L_0x004f:
        r8 = r12.journalWriter;	 Catch:{ all -> 0x0074 }
        r8.flush();	 Catch:{ all -> 0x0074 }
        r8 = r12.size;	 Catch:{ all -> 0x0074 }
        r10 = r12.maxSize;	 Catch:{ all -> 0x0074 }
        r8 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r8 <= 0) goto L_0x012d;
    L_0x005c:
        r8 = 1;
    L_0x005d:
        if (r8 != 0) goto L_0x0065;
    L_0x005f:
        r8 = r12.fileCount;	 Catch:{ all -> 0x0074 }
        r9 = r12.maxFileCount;	 Catch:{ all -> 0x0074 }
        if (r8 <= r9) goto L_0x0130;
    L_0x0065:
        r8 = r12.executorService;	 Catch:{ all -> 0x0074 }
        r9 = r12.cleanupCallable;	 Catch:{ all -> 0x0074 }
        r8.submit(r9);	 Catch:{ all -> 0x0074 }
    L_0x006c:
        monitor-exit(r12);
        return;
    L_0x006e:
        r8 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0074 }
        r8.<init>();	 Catch:{ all -> 0x0074 }
        throw r8;	 Catch:{ all -> 0x0074 }
    L_0x0074:
        r8 = move-exception;
        monitor-exit(r12);
        throw r8;
    L_0x0077:
        r8 = r2.readable;	 Catch:{ all -> 0x0074 }
        if (r8 != 0) goto L_0x000d;
    L_0x007d:
        r3 = 0;
    L_0x007e:
        r8 = r12.valueCount;	 Catch:{ all -> 0x0074 }
        if (r3 >= r8) goto L_0x000d;
    L_0x0082:
        r8 = r13.written;	 Catch:{ all -> 0x0074 }
        r8 = r8[r3];	 Catch:{ all -> 0x0074 }
        if (r8 == 0) goto L_0x0097;
    L_0x008a:
        r8 = r2.getDirtyFile(r3);	 Catch:{ all -> 0x0074 }
        r8 = r8.exists();	 Catch:{ all -> 0x0074 }
        if (r8 == 0) goto L_0x00b4;
    L_0x0094:
        r3 = r3 + 1;
        goto L_0x007e;
    L_0x0097:
        r13.abort();	 Catch:{ all -> 0x0074 }
        r8 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0074 }
        r9 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0074 }
        r9.<init>();	 Catch:{ all -> 0x0074 }
        r10 = "Newly created entry didn't create value for index ";
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r9 = r9.append(r3);	 Catch:{ all -> 0x0074 }
        r9 = r9.toString();	 Catch:{ all -> 0x0074 }
        r8.<init>(r9);	 Catch:{ all -> 0x0074 }
        throw r8;	 Catch:{ all -> 0x0074 }
    L_0x00b4:
        r13.abort();	 Catch:{ all -> 0x0074 }
        monitor-exit(r12);
        return;
    L_0x00b9:
        r1 = r2.getDirtyFile(r3);	 Catch:{ all -> 0x0074 }
        if (r14 != 0) goto L_0x00c6;
    L_0x00bf:
        deleteIfExists(r1);	 Catch:{ all -> 0x0074 }
    L_0x00c2:
        r3 = r3 + 1;
        goto L_0x000e;
    L_0x00c6:
        r8 = r1.exists();	 Catch:{ all -> 0x0074 }
        if (r8 == 0) goto L_0x00c2;
    L_0x00cc:
        r0 = r2.getCleanFile(r3);	 Catch:{ all -> 0x0074 }
        r1.renameTo(r0);	 Catch:{ all -> 0x0074 }
        r8 = r2.lengths;	 Catch:{ all -> 0x0074 }
        r6 = r8[r3];	 Catch:{ all -> 0x0074 }
        r4 = r0.length();	 Catch:{ all -> 0x0074 }
        r8 = r2.lengths;	 Catch:{ all -> 0x0074 }
        r8[r3] = r4;	 Catch:{ all -> 0x0074 }
        r8 = r12.size;	 Catch:{ all -> 0x0074 }
        r8 = r8 - r6;
        r8 = r8 + r4;
        r12.size = r8;	 Catch:{ all -> 0x0074 }
        r8 = r12.fileCount;	 Catch:{ all -> 0x0074 }
        r8 = r8 + 1;
        r12.fileCount = r8;	 Catch:{ all -> 0x0074 }
        goto L_0x00c2;
    L_0x00f0:
        r8 = 1;
        r2.readable = r8;	 Catch:{ all -> 0x0074 }
        r8 = r12.journalWriter;	 Catch:{ all -> 0x0074 }
        r9 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0074 }
        r9.<init>();	 Catch:{ all -> 0x0074 }
        r10 = "CLEAN ";
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r10 = r2.key;	 Catch:{ all -> 0x0074 }
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r10 = r2.getLengths();	 Catch:{ all -> 0x0074 }
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r10 = 10;
        r9 = r9.append(r10);	 Catch:{ all -> 0x0074 }
        r9 = r9.toString();	 Catch:{ all -> 0x0074 }
        r8.write(r9);	 Catch:{ all -> 0x0074 }
        if (r14 == 0) goto L_0x004f;
    L_0x0121:
        r8 = r12.nextSequenceNumber;	 Catch:{ all -> 0x0074 }
        r10 = 1;
        r10 = r10 + r8;
        r12.nextSequenceNumber = r10;	 Catch:{ all -> 0x0074 }
        r2.sequenceNumber = r8;	 Catch:{ all -> 0x0074 }
        goto L_0x004f;
    L_0x012d:
        r8 = 0;
        goto L_0x005d;
    L_0x0130:
        r8 = r12.journalRebuildRequired();	 Catch:{ all -> 0x0074 }
        if (r8 != 0) goto L_0x0065;
    L_0x0136:
        goto L_0x006c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.completeEdit(com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache$Editor, boolean):void");
    }

    private boolean journalRebuildRequired() {
        return this.redundantOpCount >= 2000 && this.redundantOpCount >= this.lruEntries.size();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean remove(java.lang.String r9) throws java.io.IOException {
        /*
        r8 = this;
        r4 = 0;
        monitor-enter(r8);
        r8.checkNotClosed();	 Catch:{ all -> 0x0098 }
        r8.validateKey(r9);	 Catch:{ all -> 0x0098 }
        r3 = r8.lruEntries;	 Catch:{ all -> 0x0098 }
        r0 = r3.get(r9);	 Catch:{ all -> 0x0098 }
        r0 = (com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.Entry) r0;	 Catch:{ all -> 0x0098 }
        if (r0 != 0) goto L_0x0014;
    L_0x0012:
        monitor-exit(r8);
        return r4;
    L_0x0014:
        r3 = r0.currentEditor;	 Catch:{ all -> 0x0098 }
        if (r3 != 0) goto L_0x0012;
    L_0x001a:
        r2 = 0;
    L_0x001b:
        r3 = r8.valueCount;	 Catch:{ all -> 0x0098 }
        if (r2 < r3) goto L_0x0052;
    L_0x001f:
        r3 = r8.redundantOpCount;	 Catch:{ all -> 0x0098 }
        r3 = r3 + 1;
        r8.redundantOpCount = r3;	 Catch:{ all -> 0x0098 }
        r3 = r8.journalWriter;	 Catch:{ all -> 0x0098 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0098 }
        r4.<init>();	 Catch:{ all -> 0x0098 }
        r5 = "REMOVE ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0098 }
        r4 = r4.append(r9);	 Catch:{ all -> 0x0098 }
        r5 = 10;
        r4 = r4.append(r5);	 Catch:{ all -> 0x0098 }
        r4 = r4.toString();	 Catch:{ all -> 0x0098 }
        r3.append(r4);	 Catch:{ all -> 0x0098 }
        r3 = r8.lruEntries;	 Catch:{ all -> 0x0098 }
        r3.remove(r9);	 Catch:{ all -> 0x0098 }
        r3 = r8.journalRebuildRequired();	 Catch:{ all -> 0x0098 }
        if (r3 != 0) goto L_0x009b;
    L_0x004f:
        r3 = 1;
        monitor-exit(r8);
        return r3;
    L_0x0052:
        r1 = r0.getCleanFile(r2);	 Catch:{ all -> 0x0098 }
        r3 = r1.exists();	 Catch:{ all -> 0x0098 }
        if (r3 != 0) goto L_0x0078;
    L_0x005c:
        r4 = r8.size;	 Catch:{ all -> 0x0098 }
        r3 = r0.lengths;	 Catch:{ all -> 0x0098 }
        r6 = r3[r2];	 Catch:{ all -> 0x0098 }
        r4 = r4 - r6;
        r8.size = r4;	 Catch:{ all -> 0x0098 }
        r3 = r8.fileCount;	 Catch:{ all -> 0x0098 }
        r3 = r3 + -1;
        r8.fileCount = r3;	 Catch:{ all -> 0x0098 }
        r3 = r0.lengths;	 Catch:{ all -> 0x0098 }
        r4 = 0;
        r3[r2] = r4;	 Catch:{ all -> 0x0098 }
        r2 = r2 + 1;
        goto L_0x001b;
    L_0x0078:
        r3 = r1.delete();	 Catch:{ all -> 0x0098 }
        if (r3 != 0) goto L_0x005c;
    L_0x007e:
        r3 = new java.io.IOException;	 Catch:{ all -> 0x0098 }
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0098 }
        r4.<init>();	 Catch:{ all -> 0x0098 }
        r5 = "failed to delete ";
        r4 = r4.append(r5);	 Catch:{ all -> 0x0098 }
        r4 = r4.append(r1);	 Catch:{ all -> 0x0098 }
        r4 = r4.toString();	 Catch:{ all -> 0x0098 }
        r3.<init>(r4);	 Catch:{ all -> 0x0098 }
        throw r3;	 Catch:{ all -> 0x0098 }
    L_0x0098:
        r3 = move-exception;
        monitor-exit(r8);
        throw r3;
    L_0x009b:
        r3 = r8.executorService;	 Catch:{ all -> 0x0098 }
        r4 = r8.cleanupCallable;	 Catch:{ all -> 0x0098 }
        r3.submit(r4);	 Catch:{ all -> 0x0098 }
        goto L_0x004f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.nostra13.universalimageloader.cache.disc.impl.ext.DiskLruCache.remove(java.lang.String):boolean");
    }

    private void checkNotClosed() {
        if (this.journalWriter == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    public synchronized void close() throws IOException {
        if (this.journalWriter != null) {
            Iterator i$ = new ArrayList(this.lruEntries.values()).iterator();
            while (i$.hasNext()) {
                Entry entry = (Entry) i$.next();
                if (entry.currentEditor != null) {
                    entry.currentEditor.abort();
                }
            }
            trimToSize();
            trimToFileCount();
            this.journalWriter.close();
            this.journalWriter = null;
        }
    }

    private void trimToSize() throws IOException {
        while (true) {
            if ((this.size <= this.maxSize ? 1 : null) == null) {
                remove((String) ((java.util.Map.Entry) this.lruEntries.entrySet().iterator().next()).getKey());
            } else {
                return;
            }
        }
    }

    private void trimToFileCount() throws IOException {
        while (this.fileCount > this.maxFileCount) {
            remove((String) ((java.util.Map.Entry) this.lruEntries.entrySet().iterator().next()).getKey());
        }
    }

    public void delete() throws IOException {
        close();
        Util.deleteContents(this.directory);
    }

    private void validateKey(String key) {
        if (!LEGAL_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,64}: \"" + key + "\"");
        }
    }
}
