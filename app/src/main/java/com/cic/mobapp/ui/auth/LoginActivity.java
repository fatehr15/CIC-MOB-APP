package com.cic.mobapp.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.cic.mobapp.BuildConfig;
import com.cic.mobapp.MainActivity;
import com.cic.mobapp.databinding.ActivityLoginBinding;
import com.cic.mobapp.util.Validator;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel       viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        if (viewModel.isAlreadyLoggedIn()) { goToMain(); return; }

        setupTabToggle();
        setupSignIn();
        setupRegister();
        observeViewModel();

        // Handle OAuth redirect delivered to a fresh onCreate (e.g., after browser redirect
        // when this activity was not at the top of the stack).
        handleDiscordIntent(getIntent());
    }

    // ── Tab switching ─────────────────────────────────────────────────────────

    private void setupTabToggle() {
        binding.toggleAuthMode.check(binding.btnTabSignIn.getId());
        binding.toggleAuthMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            boolean signIn = checkedId == binding.btnTabSignIn.getId();
            binding.sectionSignIn.setVisibility(signIn  ? View.VISIBLE : View.GONE);
            binding.sectionRegister.setVisibility(signIn ? View.GONE   : View.VISIBLE);
            binding.tvError.setText(null);
        });
    }

    // ── Sign in ───────────────────────────────────────────────────────────────

    private void setupSignIn() {
        // Inline validation
        TextWatcher loginWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { validateLoginForm(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.etEmail.addTextChangedListener(loginWatcher);
        binding.etPassword.addTextChangedListener(loginWatcher);
        binding.btnLoginEmail.setEnabled(false);

        binding.btnLoginEmail.setOnClickListener(v -> submitEmailLogin());
        binding.etPassword.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { submitEmailLogin(); return true; }
            return false;
        });
        binding.btnLoginDiscord.setOnClickListener(v -> openDiscordOAuth());
    }

    private void validateLoginForm() {
        String email    = text(binding.etEmail);
        String password = text(binding.etPassword);
        boolean emailOk = Validator.isValidEmail(email);
        boolean passOk  = Validator.isValidPassword(password);

        binding.tilEmail.setError(!emailOk && !email.isEmpty() ? "Invalid email address" : null);
        binding.tilPassword.setError(!passOk && !password.isEmpty() ? "Min 8 characters" : null);
        binding.btnLoginEmail.setEnabled(emailOk && passOk);
    }

    private void submitEmailLogin() {
        viewModel.loginWithEmail(text(binding.etEmail), text(binding.etPassword));
    }

    // ── Register ──────────────────────────────────────────────────────────────

    private void setupRegister() {
        TextWatcher regWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { validateRegisterForm(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        binding.etUsername.addTextChangedListener(regWatcher);
        binding.etRegEmail.addTextChangedListener(regWatcher);
        binding.etRegPassword.addTextChangedListener(regWatcher);
        binding.etConfirmPassword.addTextChangedListener(regWatcher);
        binding.btnRegister.setEnabled(false);

        binding.btnRegister.setOnClickListener(v -> submitRegister());
        binding.etConfirmPassword.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { submitRegister(); return true; }
            return false;
        });
    }

    private void validateRegisterForm() {
        String username = text(binding.etUsername);
        String email    = text(binding.etRegEmail);
        String password = text(binding.etRegPassword);
        String confirm  = text(binding.etConfirmPassword);

        boolean emailOk   = Validator.isValidEmail(email);
        boolean passOk    = Validator.isValidPassword(password);
        boolean matchOk   = password.equals(confirm);
        boolean userOk    = username.length() >= 3;

        binding.tilRegEmail.setError(!emailOk && !email.isEmpty() ? "Invalid email" : null);

        int strength = Validator.passwordStrength(password);
        binding.tilRegPassword.setError(!passOk && !password.isEmpty()
                ? Validator.passwordStrengthLabel(strength) : null);

        binding.tilConfirmPassword.setError(!matchOk && !confirm.isEmpty() ? "Passwords do not match" : null);
        binding.btnRegister.setEnabled(userOk && emailOk && passOk && matchOk);
    }

    private void submitRegister() {
        viewModel.register(text(binding.etUsername), text(binding.etRegEmail),
                text(binding.etRegPassword), text(binding.etConfirmPassword));
    }

    // ── Discord OAuth2 ────────────────────────────────────────────────────────

    private void openDiscordOAuth() {
        String url = "https://discord.com/api/oauth2/authorize"
                + "?client_id=" + BuildConfig.DISCORD_CLIENT_ID
                + "&redirect_uri=" + Uri.encode(BuildConfig.DISCORD_REDIRECT_URI)
                + "&response_type=code&scope=identify%20email%20guilds";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleDiscordIntent(intent);
    }

    private void handleDiscordIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "cic".equals(data.getScheme())) {
            String code = data.getQueryParameter("code");
            if (code != null) viewModel.handleDiscordCallback(code);
        }
    }

    // ── Observe ───────────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getLoginSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) goToMain();
        });

        viewModel.getError().observe(this, error ->
                binding.tvError.setText(error != null ? error : ""));

        viewModel.getLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLoginEmail.setEnabled(!loading && isLoginFormValid());
            binding.btnLoginDiscord.setEnabled(!loading);
            binding.btnRegister.setEnabled(!loading && isRegisterFormValid());
        });
    }

    private boolean isLoginFormValid() {
        return Validator.isValidEmail(text(binding.etEmail))
                && Validator.isValidPassword(text(binding.etPassword));
    }

    private boolean isRegisterFormValid() {
        String p = text(binding.etRegPassword);
        String c = text(binding.etConfirmPassword);
        return text(binding.etUsername).length() >= 3
                && Validator.isValidEmail(text(binding.etRegEmail))
                && Validator.isValidPassword(p) && p.equals(c);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String text(com.google.android.material.textfield.TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
