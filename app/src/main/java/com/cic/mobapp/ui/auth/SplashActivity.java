package com.cic.mobapp.ui.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cic.mobapp.MainActivity;
import com.cic.mobapp.databinding.ActivitySplashBinding;
import com.cic.mobapp.util.TokenManager;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String[] BOOT_LINES = {
        "Initializing CIC Environment...",
        "Loading Community Nodes...",
        "Synchronizing Resources...",
        "Access Granted."
    };

    // Timing (ms)
    private static final int LOGO_FADE_DURATION  = 600;
    private static final int LOGO_HOLD           = 400;
    private static final int CHAR_DELAY          = 28;
    private static final int LINE_GAP            = 180;
    private static final int FINAL_HOLD          = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Determine destination before animation starts
        boolean isLoggedIn = new TokenManager(this).isLoggedIn();

        fadeLogo(() -> startBootSequence(isLoggedIn));
    }

    // ── Logo fade-in ──────────────────────────────────────────────────────────

    private void fadeLogo(Runnable onDone) {
        binding.tvLogo.animate()
                .alpha(1f)
                .setDuration(LOGO_FADE_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator a) {
                        binding.tvTagline.animate()
                                .alpha(1f)
                                .setDuration(400)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override public void onAnimationEnd(Animator a) {
                                        handler.postDelayed(onDone, LOGO_HOLD);
                                    }
                                });
                    }
                });
    }

    // ── Boot sequence ─────────────────────────────────────────────────────────

    private void startBootSequence(boolean isLoggedIn) {
        TextView[] lineViews = {
            binding.tvLine1,
            binding.tvLine2,
            binding.tvLine3,
            binding.tvLine4
        };

        typeLines(lineViews, 0, () ->
                handler.postDelayed(() -> navigate(isLoggedIn), FINAL_HOLD));
    }

    private void typeLines(TextView[] views, int index, Runnable onAllDone) {
        if (index >= views.length) {
            onAllDone.run();
            return;
        }
        TextView tv = views[index];
        tv.setVisibility(View.VISIBLE);
        tv.setText("");

        typeText(tv, BOOT_LINES[index], 0, () ->
                handler.postDelayed(
                        () -> typeLines(views, index + 1, onAllDone),
                        LINE_GAP));
    }

    private void typeText(TextView tv, String fullText, int charIndex, Runnable onDone) {
        if (charIndex > fullText.length()) {
            onDone.run();
            return;
        }
        tv.setText(fullText.substring(0, charIndex));
        handler.postDelayed(() -> typeText(tv, fullText, charIndex + 1, onDone), CHAR_DELAY);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void navigate(boolean isLoggedIn) {
        Intent intent = isLoggedIn
                ? new Intent(this, MainActivity.class)
                : new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
