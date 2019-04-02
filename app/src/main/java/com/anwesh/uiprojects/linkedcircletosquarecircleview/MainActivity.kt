package com.anwesh.uiprojects.linkedcircletosquarecircleview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circletosquarecircleview.CircleToSquareCircleView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircleToSquareCircleView.create(this)
    }
}
