package com.mediatek.watchapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MoonPhase {
    static final /* synthetic */ boolean assertionsDisabled = (!MoonPhase.class.desiredAssertionStatus());
    private SQLiteOpenHelper dbHelper;
    private final Map<String, Integer> monthNumbers = new HashMap();
    private final int[] phaseOffsetKeys = new int[]{0, 2, 4, 6};
    private final Map<Integer, Integer> phaseOffsets = new HashMap();

    private class MoonPhaseDBHelper extends SQLiteOpenHelper {
        MoonPhaseDBHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, "moonPhase.db", factory, version);
        }

        public void onCreate(SQLiteDatabase database) {
            database.execSQL("create table if not exists moon_phase (phase integer(3) not null,occurs_at char(20) primary key);");
        }

        public void onUpgrade(SQLiteDatabase db, int i, int j) {
            onCreate(db);
        }
    }

    public MoonPhase(Context context) {
        if (this.dbHelper == null) {
            this.dbHelper = new MoonPhaseDBHelper(context, null, null, 1);
        }
        initPahases(context);
    }

    public int searchMoonPhase() {
        Calendar when = Calendar.getInstance(Locale.getDefault());
        when.setTimeInMillis(System.currentTimeMillis());
        when.setTimeZone(TimeZone.getDefault());
        when.set(Calendar.HOUR_OF_DAY, 0);
        when.set(Calendar.MINUTE, 0);
        when.set(Calendar.SECOND, 0);
        int month = when.get(Calendar.MONTH) + 1;
        int day = when.get(Calendar.DAY_OF_MONTH);
        String searchTime = DateFormat.format("yyyy-MM-dd kk:mm:ss", when).toString();
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.query("moon_phase", null, "occurs_at>=?", new String[]{searchTime}, null, null, "occurs_at ASC", "1");
        if (cursor.getCount() == 0) {
            cursor.close();
            db.endTransaction();
            db.close();
            return 4;
        }
        cursor.moveToFirst();
        int phase = cursor.getInt(cursor.getColumnIndex("phase"));
        Log.d("found phase", "=" + phase);
        String occurs_at = cursor.getString(cursor.getColumnIndex("occurs_at"));
        int searchMon = Integer.parseInt(occurs_at.substring(5, 7));
        int searchDay = Integer.parseInt(occurs_at.substring(8, 10));
        cursor.close();
        db.endTransaction();
        db.close();
        if (searchMon != month) {
            if (phase == 0) {
                return 7;
            }
            return phase - 1;
        } else if (searchDay == day) {
            return phase;
        } else {
            if (phase == 0) {
                return 7;
            }
            return phase - 1;
        }
    }

    private void initPahases(Context context) {
        IOException e;
        Throwable th;
        Calendar when = Calendar.getInstance(Locale.getDefault());
        when.setTimeInMillis(System.currentTimeMillis());
        when.setTimeZone(TimeZone.getDefault());
        int year = when.get(Calendar.YEAR);
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor cursor = null;
        try {
            Log.d("reading DB", "year:" + year);
            cursor = db.rawQuery("select * from moon_phase where occurs_at like '%" + year + "%'", null);
            if (cursor.getCount() == 0) {
                AssetManager assetMgr = context.getAssets();
                String[] assets = null;
                try {
                    assets = assetMgr.list("LoggingEvents.EXTRA_CALLING_APP_NAME");
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                if (assets != null) {
                    Log.d("reading DB", "populating :" + year);
                    ContentValues rowData = new ContentValues();
                    if (this.phaseOffsets.isEmpty()) {
                        this.phaseOffsets.put(Integer.valueOf(this.phaseOffsetKeys[0]), Integer.valueOf(4));
                        this.phaseOffsets.put(Integer.valueOf(this.phaseOffsetKeys[1]), Integer.valueOf(20));
                        this.phaseOffsets.put(Integer.valueOf(this.phaseOffsetKeys[2]), Integer.valueOf(36));
                        this.phaseOffsets.put(Integer.valueOf(this.phaseOffsetKeys[3]), Integer.valueOf(52));
                    }
                    if (this.monthNumbers.isEmpty()) {
                        this.monthNumbers.put("Jan", Integer.valueOf(0));
                        this.monthNumbers.put("Feb", Integer.valueOf(1));
                        this.monthNumbers.put("Mar", Integer.valueOf(2));
                        this.monthNumbers.put("Apr", Integer.valueOf(3));
                        this.monthNumbers.put("May", Integer.valueOf(4));
                        this.monthNumbers.put("Jun", Integer.valueOf(5));
                        this.monthNumbers.put("Jul", Integer.valueOf(6));
                        this.monthNumbers.put("Aug", Integer.valueOf(7));
                        this.monthNumbers.put("Sep", Integer.valueOf(8));
                        this.monthNumbers.put("Oct", Integer.valueOf(9));
                        this.monthNumbers.put("Nov", Integer.valueOf(10));
                        this.monthNumbers.put("Dec", Integer.valueOf(11));
                    }
                    String name = "phases_" + year + ".txt";
                    BufferedReader bufferedReader = null;
                    try {
                        Log.d("reading DB", name);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(assetMgr.open(name)));
                        while (true) {
                            try {
                                String mLine = reader.readLine();
                                if (mLine == null) {
                                    break;
                                }
                                Log.d("reading DB", mLine);
                                loadPhase(db, when, mLine, rowData);
                            } catch (IOException e3) {
                                bufferedReader = reader;
                            } catch (Throwable th2) {
                                bufferedReader = reader;
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e4) {
                            }
                        }
                    } catch (IOException e5) {
                        try {
                            Log.d("reading DB failed", e5.getMessage());
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e6) {
                                }
                            }
                            db.setTransactionSuccessful();
                            cursor.close();
                            db.endTransaction();
                            db.close();
                        } catch (Throwable th3) {
                            if (bufferedReader != null) {
                                try {
                                    bufferedReader.close();
                                } catch (IOException e7) {
                                }
                            }
                            throw th3;
                        }
                    }
                    db.setTransactionSuccessful();
                } else {
                    return;
                }
            }
            cursor.close();
            db.endTransaction();
            db.close();
        } catch (SQLiteException e8) {
            if (!assertionsDisabled) {
                Object obj;
                if (cursor != null) {
                    obj = 1;
                } else {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            cursor.close();
            db.endTransaction();
            db.close();
        }
    }

    private void loadPhase(SQLiteDatabase db, Calendar when, String line, ContentValues rowData) {
        for (int phaseName : this.phaseOffsetKeys) {
            int offset = (Integer) this.phaseOffsets.get(Integer.valueOf(phaseName));
            if (line.length() >= offset + 3) {
                if (!line.substring(offset, offset + 3).trim().isEmpty()) {
                    int month = (Integer) this.monthNumbers.get(line.substring(offset, offset + 3).trim());
                    int day = Integer.valueOf(line.substring(offset + 4, offset + 6).trim());
                    int hour = Integer.valueOf(line.substring(offset + 7, offset + 9).trim());
                    int minute = Integer.valueOf(line.substring(offset + 10, offset + 12).trim());
                    when.set(Calendar.MONTH, month);
                    when.set(Calendar.DAY_OF_MONTH, day);
                    when.set(Calendar.HOUR_OF_DAY, hour);
                    when.set(Calendar.MINUTE, minute);
                    when.set(Calendar.SECOND, 0);
                    when.set(Calendar.MILLISECOND, 0);
                    String moon_phase = DateFormat.format("yyyy-MM-dd kk:mm:ss", when).toString();
                    rowData.put("phase", Integer.valueOf(phaseName));
                    rowData.put("occurs_at", moon_phase);
                    db.insert("moon_phase", "LoggingEvents.EXTRA_CALLING_APP_NAME", rowData);
                }
            }
        }
    }
}
