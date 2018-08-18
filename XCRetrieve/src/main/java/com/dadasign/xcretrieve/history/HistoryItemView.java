package com.dadasign.xcretrieve.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.dadasign.xcretrieve.R;

/**
 * Created by Jakub on 2015-09-23.
 */
public class HistoryItemView extends RelativeLayout implements Checkable {
    private View v;
    private boolean isCheckedStatus;
    private static final int[] mCheckedStateSet = {
            android.R.attr.state_checked,
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public HistoryItemView(Context context){
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        v = inflater.inflate(R.layout.history_list_item, this, true);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, mCheckedStateSet);
        }
        return drawableState;
    }

    @Override
    public void setChecked(boolean b) {
        isCheckedStatus=b;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return isCheckedStatus;
    }

    @Override
    public void toggle() {
        isCheckedStatus = !isCheckedStatus;
    }
}
