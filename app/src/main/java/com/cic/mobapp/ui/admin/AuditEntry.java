package com.cic.mobapp.ui.admin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuditEntry {

    // ── Action constants ──────────────────────────────────────────────────────
    public static final String CREATE  = "CREATED";
    public static final String UPDATE  = "UPDATED";
    public static final String DELETE  = "DELETED";
    public static final String ROLE    = "ROLE CHANGE";
    public static final String WARN    = "WARNING ISSUED";
    public static final String MUTE    = "USER MUTED";
    public static final String SUSPEND = "SUSPENDED";
    public static final String CANCEL  = "CANCELED";
    public static final String BAN     = "BANNED";

    // ── Severity constants ────────────────────────────────────────────────────
    public static final String INFO     = "INFO";
    public static final String WARNING  = "WARNING";
    public static final String CRITICAL = "CRITICAL";
    public static final String SECURITY = "SECURITY";

    public final String action;
    public final String entity;
    public final String detail;
    public final String time;
    public final String date;
    public final String severity;

    public AuditEntry(String action, String entity, String detail) {
        this.action   = action;
        this.entity   = entity;
        this.detail   = detail;
        Date now = new Date();
        this.time     = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now);
        this.date     = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
        this.severity = deriveSeverity(action);
    }

    private static String deriveSeverity(String action) {
        switch (action != null ? action : "") {
            case BAN:
            case MUTE:
            case SUSPEND: return CRITICAL;
            case DELETE:
            case WARN:
            case CANCEL:
            case ROLE:    return WARNING;
            default:      return INFO;
        }
    }
}
