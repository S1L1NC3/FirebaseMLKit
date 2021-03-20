package com.dmd.samplelibrary

import android.content.Context
import android.widget.Toast

class ToastHelper{
    fun shortShow(textToShow: String, context: Context){
        Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show()
    }
}