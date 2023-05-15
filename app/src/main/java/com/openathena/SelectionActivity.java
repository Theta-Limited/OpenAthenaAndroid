package com.openathena;

import android.os.Bundle;
import android.view.View;

public class SelectionActivity extends AthenaActivity{
    public static String TAG = SelectionActivity.class.getSimpleName();
    private MarkableImageView iView;
    /**
     * All the important point selection logic is handled by parent, so we don't change much here
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        iView = (MarkableImageView) findViewById(R.id.selection_image_view);

        restorePrefOutputMode();
    }

    @Override
    public void calculateImage(View view) {
        return; // do nothing
    }

    @Override
    public void calculateImage(View view, boolean shouldISendCoT) {
        return; // do nothing
    }
}
