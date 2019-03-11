package com.ken.arsensorduino.util;

public class MathUtil {

    public static float map(float v,float origMin,float origMax,float min,float max){
        if(min>max){
            float t=max;
            max=min;
            min=t;
            t=origMin;
            origMin=origMax;
            origMax=t;
        }
        if(v==origMin){
            return min;
        }
        if(v==origMax){
            return max;
        }
        v= (v-origMin)/(origMax-origMin)*(max-min)+min;
        return Math.min(Math.max(v,min),max);
    }
}
