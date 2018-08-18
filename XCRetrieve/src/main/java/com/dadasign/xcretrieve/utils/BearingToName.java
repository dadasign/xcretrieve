package com.dadasign.xcretrieve.utils;

import android.text.Html;

/**
 * Created by Jakub on 2015-07-20.
 */
public class BearingToName {
    public String convertBearing(int dir){
        if(dir<0){
            dir+=360;
        }
        String dir_text;
        String degree = Html.fromHtml("&deg;").toString();
        if(dir <= 11 || dir>348){
            dir_text=dir+degree+" (N)";
        }else if(dir<34){
            dir_text=dir+degree+" (NNE)";
        }else if(dir<57){
            dir_text=dir+degree+" (NE)";
        }else if(dir<57){
            dir_text=dir+degree+" (NE)";
        }else if(dir<79){
            dir_text=dir+degree+" (ENE)";
        }else if(dir<102){
            dir_text=dir+degree+" (E)";
        }else if(dir<123){
            dir_text=dir+degree+" (ESE)";
        }else if(dir<146){
            dir_text=dir+degree+" (SE)";
        }else if(dir<169){
            dir_text=dir+degree+" (SSE)";
        }else if(dir<192){
            dir_text=dir+degree+" (S)";
        }else if(dir<214){
            dir_text=dir+degree+" (SSW)";
        }else if(dir<237){
            dir_text=dir+degree+" (SW)";
        }else if(dir<259){
            dir_text=dir+degree+" (WSW)";
        }else if(dir<282){
            dir_text=dir+degree+" (W)";
        }else if(dir<304){
            dir_text=dir+degree+" (WNW)";
        }else if(dir<327){
            dir_text=dir+degree+" (NW)";
        }else{
            dir_text=dir+degree+" (NNW)";
        }
        return dir_text;
    }
}
