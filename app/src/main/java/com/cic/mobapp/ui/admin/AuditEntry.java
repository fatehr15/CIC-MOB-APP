package com.cic.mobapp.ui.admin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuditEntry {

    public static final String CREATE = "CREATED";
    public static final String UPDATE = "UPDATED";
    public static final String DELETE = "DELETED";
    public static final String ROLE   = "ROLE CHANGE";

    public final String action;
    public final String entity;
    public final String detail;
    public final String time;

    public AuditEntry(String action, String entity, String detail) {
        this.action = action;
        this.entity = entity;
        this.detail = detail;
        this.time   = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
