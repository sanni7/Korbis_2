package com.tbc_hackathon.dailyhunt.korbis_2;

import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by srisannidhi on 30/1/18.
 */

public class HourOfTheDayUtility
{
    static Calendar c = Calendar.getInstance();
    static int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

    static String get()
    {
        if(timeOfDay >= 0 && timeOfDay < 12)
        {
            return "Morning";
        }
        else if(timeOfDay >= 12 && timeOfDay < 16)
        {
            return "Noon";
        }
        else if(timeOfDay >= 16 && timeOfDay < 21)
        {
            return "Evening";
        }
        else if(timeOfDay >= 21 && timeOfDay < 24)
        {
            return "Night";
        }
        return "";
    }
}
