package com.cic.mobapp.ui.events;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.cic.mobapp.R;
import com.cic.mobapp.data.local.entity.EventEntity;
import com.cic.mobapp.databinding.ActivityEventDetailBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EventDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private ActivityEventDetailBinding binding;
    private EventDetailViewModel viewModel;
    private EventEntity currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(EventDetailViewModel.class);

        viewModel.getEvent(eventId).observe(this, event -> {
            if (event == null) return;
            currentEvent = event;
            bindEvent(event);
        });

        viewModel.getToast().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        binding.btnCalendar.setOnClickListener(v -> addToCalendar());
        binding.btnShare.setOnClickListener(v -> shareEvent());
        binding.btnRegister.setOnClickListener(v -> {
            if (currentEvent != null) viewModel.toggleRegistration(currentEvent);
        });
    }

    private void bindEvent(EventEntity e) {
        binding.toolbar.setTitle(e.type);

        Glide.with(this).load(e.bannerUrl).centerCrop()
                .placeholder(R.drawable.placeholder_banner)
                .into(binding.imgBanner);

        binding.tvType.setText(e.type);
        binding.tvType.setTextColor(typeColor(e.type));
        binding.tvDifficultyBadge.setText(e.difficulty);
        binding.tvDifficultyBadge.setTextColor(difficultyColor(e.difficulty));

        binding.tvEventTitle.setText(e.title);
        binding.tvEventDate.setText(formatDate(e.date));
        binding.tvEventLocation.setText(e.location);
        binding.tvEventDescription.setText(e.description);

        int registered = e.registeredCount;
        int capacity   = Math.max(e.capacity, 1);
        int seatsLeft  = capacity - registered;
        int percent    = (int)(((float) registered / capacity) * 100);

        binding.progressCapacity.setProgress(percent);
        binding.tvSeats.setText(seatsLeft + " / " + capacity + " seats");

        boolean full = seatsLeft <= 0;
        binding.tvRegistrationStatus.setText(
                e.isRegistered ? "You are registered ✓" :
                full           ? "Event is full"         :
                                 "Open for registration");

        binding.btnRegister.setText(e.isRegistered ? "Cancel Registration" : "Register Now");
        binding.btnRegister.setEnabled(!full || e.isRegistered);
        int btnColor = e.isRegistered
                ? getResources().getColor(R.color.status_error, getTheme())
                : getResources().getColor(R.color.accent_cyan, getTheme());
        binding.btnRegister.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(btnColor));
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void addToCalendar() {
        if (currentEvent == null) return;
        long startMs = parseIsoToMillis(currentEvent.date);
        Intent cal = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE,          currentEvent.title)
                .putExtra(CalendarContract.Events.DESCRIPTION,    currentEvent.description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, currentEvent.location)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,   startMs + 2 * 60 * 60 * 1000L);
        if (cal.resolveActivity(getPackageManager()) != null) startActivity(cal);
        else Toast.makeText(this, "No calendar app found", Toast.LENGTH_SHORT).show();
    }

    private void shareEvent() {
        if (currentEvent == null) return;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, currentEvent.title);
        share.putExtra(Intent.EXTRA_TEXT,
                currentEvent.title + "\n" + formatDate(currentEvent.date)
                        + "\n" + currentEvent.location
                        + "\n\n" + currentEvent.description
                        + "\n\nShared from CIC App");
        startActivity(Intent.createChooser(share, "Share event via"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static long parseIsoToMillis(String iso) {
        if (iso == null) return System.currentTimeMillis();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(iso);
            return d != null ? d.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        try {
            String[] parts = iso.split("T");
            String[] d     = parts[0].split("-");
            String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                               "Jul","Aug","Sep","Oct","Nov","Dec"};
            String month = months[Integer.parseInt(d[1]) - 1];
            String time  = parts.length > 1 && parts[1].length() >= 5
                    ? "  ·  " + parts[1].substring(0, 5) : "";
            return month + " " + d[2] + ", " + d[0] + time;
        } catch (Exception e) {
            return iso.substring(0, 10);
        }
    }

    private static int typeColor(String type) {
        if (type == null) return Color.parseColor("#00D1FF");
        switch (type) {
            case "CTF":        return Color.parseColor("#EF4444");
            case "Workshop":   return Color.parseColor("#00D1FF");
            case "Bootcamp":   return Color.parseColor("#F59E0B");
            case "Conference": return Color.parseColor("#8B5CF6");
            case "Meeting":    return Color.parseColor("#22C55E");
            default:           return Color.parseColor("#00D1FF");
        }
    }

    private static int difficultyColor(String diff) {
        if (diff == null) return Color.parseColor("#8B9AB0");
        switch (diff) {
            case "Beginner":     return Color.parseColor("#22C55E");
            case "Intermediate": return Color.parseColor("#F59E0B");
            case "Advanced":     return Color.parseColor("#EF4444");
            default:             return Color.parseColor("#8B9AB0");
        }
    }
}
