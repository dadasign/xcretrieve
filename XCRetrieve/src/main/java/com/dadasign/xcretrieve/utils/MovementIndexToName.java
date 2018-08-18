package com.dadasign.xcretrieve.utils;

import android.content.Context;

import com.dadasign.xcretrieve.R;

/**
 * Created by Jakub on 2015-10-13.
 */
public class MovementIndexToName {
    Context ctx;
    public MovementIndexToName(Context _ctx){
        ctx=_ctx;
    }
    public String getMovementName(int movement){
        String movement_txt;
        switch(movement){
            case 0:
                return ctx.getResources().getString(R.string.movement_0);
            case 1:
                return ctx.getResources().getString(R.string.movement_1);
            case 2:
                return ctx.getResources().getString(R.string.movement_2);
            case 3:
                return ctx.getResources().getString(R.string.movement_3);
            case 4:
                return ctx.getResources().getString(R.string.movement_4);
            case 5:
                return ctx.getResources().getString(R.string.movement_5);
            case 6:
                return ctx.getResources().getString(R.string.movement_6);
        }
        return "";
    }
}
