package com.android.settings.biometrics.eye;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.settings.R;
import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupdesign.GlifLayout;

import java.util.List;

public class EyeEnrollActivity extends AppCompatActivity {

    private GlifLayout mGlifLayout;
    private ProgressBar mProgressBar;
    private TextView mStatusText;
    private int mProgress = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eye_enroll_layout);

        mUserId = getIntent().getIntExtra(Intent.EXTRA_USER_ID, 0);

        mGlifLayout = findViewById(R.id.glif_layout);
        mProgressBar = findViewById(R.id.enroll_progress);
        mStatusText = findViewById(R.id.status_text);

        if (mGlifLayout != null) {
            mGlifLayout.setHeaderText(R.string.eye_enroll_title);
            mGlifLayout.setDescriptionText(R.string.eye_enroll_summary);

            FooterBarMixin footerBarMixin = mGlifLayout.getMixin(FooterBarMixin.class);
            if (footerBarMixin != null) {
                footerBarMixin.setSecondaryButton(
                        new FooterButton.Builder(this)
                                .setText(R.string.cancel)
                                .setListener(v -> finish())
                                .setButtonType(FooterButton.ButtonType.CANCEL)
                                .build()
                );
            }
        }

        startScanningAnimation();
    }

    private void startScanningAnimation() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) return;
                mProgress += 20;
                if (mProgressBar != null) {
                    mProgressBar.setProgress(mProgress);
                }
                if (mProgress < 100) {
                    if (mStatusText != null) {
                        mStatusText.setText(R.string.eye_enroll_look_camera);
                    }
                    mHandler.postDelayed(this, 500);
                } else {
                    completeEnrollment();
                }
            }
        }, 600);
    }

    private void completeEnrollment() {
        List<EyeTemplate> eyes = EyeUtils.getEnrolledEyes(this, mUserId);
        int nextId = 1;
        for (EyeTemplate e : eyes) {
            if (e.getId() >= nextId) {
                nextId = e.getId() + 1;
            }
        }

        String name = getString(R.string.eye_default_name, nextId);
        EyeUtils.addEye(this, nextId, name, mUserId);

        Vibrator vibrator = getSystemService(Vibrator.class);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(120);
            }
        }

        if (mStatusText != null) {
            mStatusText.setText(R.string.eye_enroll_done);
        }

        mHandler.postDelayed(() -> {
            setResult(Activity.RESULT_OK);
            finish();
        }, 800);
    }
}
