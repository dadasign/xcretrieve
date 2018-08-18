package com.dadasign.xcretrieve.utils;

import com.dadasign.xcretrieve.R;

/**
 * Created by Jakub on 2015-07-20.
 */
public class MovementIndexNames {
    public int getStringIndentificatorForIndex(int i){
        switch(i){
            case 0:
               return R.string.movement_0;
            case 1:
                return R.string.movement_1;
            case 2:
                return R.string.movement_2;
            case 3:
                return R.string.movement_3;
            case 4:
                return R.string.movement_4;
            case 5:
                return R.string.movement_5;
            case 6:
                return R.string.movement_6;
            default:
                return R.string.movement_unknown;
        }
    }
}
