package com.dmd.helpers

import android.util.Log

class LogHelper {
    companion object{
        fun log(textToLog: String){
            Log.d("MertTrackLog",textToLog)
        }

        fun log(intToLog: Int){
            Log.d("MertTrackLog", intToLog.toString())
        }

        fun log(doubleToLog: Double){
            Log.d("MertTrackLog", doubleToLog.toString())
        }

        fun log(doubleToLog: Long){
            Log.d("MertTrackLog", doubleToLog.toString())
        }

    }
}