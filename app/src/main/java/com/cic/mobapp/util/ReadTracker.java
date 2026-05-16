package com.cic.mobapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

/** Tracks which announcement IDs the user has already opened. */
public class ReadTracker {

    private static final String PREF = "cic_read_announcements";
    private static final String KEY  = "read_ids";

    private final SharedPreferences prefs;

    public ReadTracker(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public boolean isRead(String id) {
        return getReadIds().contains(id);
    }

    public void markRead(String id) {
        Set<String> ids = new HashSet<>(getReadIds());
        ids.add(id);
        prefs.edit().putStringSet(KEY, ids).apply();
    }

    private Set<String> getReadIds() {
        return prefs.getStringSet(KEY, new HashSet<>());
    }
}
