package com.dmd.helpers

import android.content.Context
import android.widget.Toast

class ToastHelper{
    companion object{
        fun shortShow(context: Context, textToShow: String){
            Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show()
        }

        fun longShow(context: Context, textToShow: String){
            Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show()
        }
    }
}