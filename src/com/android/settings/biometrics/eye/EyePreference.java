package com.android.settings.biometrics.eye;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;
import com.android.settingslib.widget.TwoTargetPreference;

public class EyePreference extends TwoTargetPreference {

    public interface OnDeleteClickListener {
        void onDeleteClick(EyePreference preference);
    }

    private EyeTemplate mEye;
    private OnDeleteClickListener mDeleteClickListener;

    public EyePreference(@NonNull Context context) {
        super(context);
    }

    public EyePreference(@NonNull Context context, OnDeleteClickListener deleteClickListener) {
        super(context);
        mDeleteClickListener = deleteClickListener;
    }

    public EyePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EyePreference(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EyePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setEye(EyeTemplate eye) {
        mEye = eye;
    }

    public EyeTemplate getEye() {
        return mEye;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        mDeleteClickListener = listener;
    }

    @Override
    protected int getSecondTargetResId() {
        return R.layout.preference_widget_delete;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        final View divider = view.findViewById(
                com.android.settingslib.widget.preference.twotarget.R.id.two_target_divider);
        if (divider != null) {
            divider.setVisibility(View.GONE);
        }

        final View deleteIcon = view.findViewById(R.id.delete_button);
        if (deleteIcon != null) {
            deleteIcon.setBackground(null);
            deleteIcon.setOnClickListener(v -> {
                if (mDeleteClickListener != null) {
                    mDeleteClickListener.onDeleteClick(this);
                }
            });
        }
    }
}
