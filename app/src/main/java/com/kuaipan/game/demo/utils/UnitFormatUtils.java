package com.kuaipan.game.demo.utils;

import android.content.Context;

import com.kuaipan.game.demo.R;

import java.text.DecimalFormat;


public class UnitFormatUtils {
    public static String formatPerson(Context context, int count) {
        if (count < 10000) {
            return context.getString(R.string.person_unit_level1, String.valueOf(count));
        } else if (count < 100000000) {
            return context.getString(R.string.person_unit_level2, String.valueOf(count / 10000));
        } else {
            return context.getString(R.string.person_unit_level3, String.valueOf(1));
        }
    }

    public static String formatDate(Context context, long date) {
        if (date < 60) {
            return context.getString(R.string.time_unit_second, String.valueOf(date));
        } else if (date < 60 * 60) {
            return context.getString(R.string.time_unit_min, String.valueOf(date / 60));
        } else if (date < 24 * 60 * 60) {
            int hour = (int) (date / 60 / 60);
            int min = (int) ((date / 60) % 60);
            if (min != 0) {
                return context.getString(R.string.time_unit_hour_and_min, String.valueOf(hour), String.valueOf(min));
            } else {
                return context.getString(R.string.time_unit_hour, String.valueOf(hour));
            }
        } else {
            int day = (int) (date / 60 / 60 / 24);
            int hour = (int) ((date / 60 / 60) % 24);
            if (hour != 0) {
                return context.getString(R.string.time_unit_day_and_hour, String.valueOf(day), String.valueOf(hour));
            } else {
                return context.getString(R.string.time_unit_day, String.valueOf(day));
            }
        }
    }

    public static String formatBytes(long size, boolean hasByte) {
        if (size >= 1048051712l) { // 1048051712l=1024*1024*999.5
            // in GB
            float gbSize = size / (1024 * 1024 * 1024f);
            DecimalFormat formatter = getFormat(gbSize);
            return formatter.format(gbSize) + "G" + (hasByte ? "B" : "");
        } else if (size >= 1023488l) { // 1023488=1024*999.5
            // in MB
            float mbSize = size / (1024 * 1024f);
            DecimalFormat formatter = getFormat(mbSize);
            return formatter.format(mbSize) + "M" + (hasByte ? "B" : "");
        } else if (size >= 1000) {
            // in KB
            float kbSize = size / 1024f;
            DecimalFormat formatter = getFormat(kbSize);
            return formatter.format(kbSize) + "K" + (hasByte ? "B" : "");
        } else {
            return size + "B";
        }
    }

    /*
    * size must less than 1000
    */
    private static DecimalFormat getFormat(float size) {
        DecimalFormat decimal;
        if (size < 10) {
            decimal = new DecimalFormat("#0.0");
        } else if (size < 100) {
            decimal = new DecimalFormat("#0.0");
        } else {
            decimal = new DecimalFormat("#0");
        }
        return decimal;
    }
}
