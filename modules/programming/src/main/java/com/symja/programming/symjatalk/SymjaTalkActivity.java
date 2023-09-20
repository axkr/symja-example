package com.symja.programming.symjatalk;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.symja.common.datastrcture.Data;
import com.symja.programming.BaseActivity;
import com.symja.programming.R;

public class SymjaTalkActivity extends BaseActivity {

    private static final String KEY_INPUT = "input";

    public static void open(@NonNull Fragment fragment,
                            @Nullable Data input) {
        Intent intent = new Intent(fragment.getContext(), SymjaTalkActivity.class);
        if (input != null) {
            intent.putExtra(KEY_INPUT, input);
        }
        fragment.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symja_prgm_activity_with_fragment_content);
        setupToolbar();
        setTitle(R.string.symja_prgm_title_expression_details);

        Data data = null;
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_INPUT)) {
            data = (Data) intent.getSerializableExtra(KEY_INPUT);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content,
                        SymjaTalkFragment.newInstance(data))
                .commitAllowingStateLoss();
    }
}
