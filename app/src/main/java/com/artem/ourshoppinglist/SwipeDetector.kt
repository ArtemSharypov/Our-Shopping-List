package com.artem.ourshoppinglist

import android.view.MotionEvent
import android.view.View

class SwipeDetector : View.OnTouchListener {
    enum class Action {
        LR, // Left to Right
        RL, // Right to Left
        TB, // Top to bottom
        BT, // Bottom to Top
        None // when no action was detected
    }

    private val MIN_DISTANCE = 100
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var upX: Float = 0f
    private var upY: Float = 0f
    private var mSwipeDetected = Action.None
    private var lastTouchTime: Long = -1

    fun swipeDetected(): Boolean {
        return mSwipeDetected != Action.None
    }

    fun getAction(): Action {
        return mSwipeDetected
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
       when(event?.action) {
           MotionEvent.ACTION_UP -> {
               upX = event.x
               upY = event.y

               val deltaX = downX - upX
               val deltaY = downY - upY

               // horizontal swipe detection
               if (Math.abs(deltaX) > MIN_DISTANCE) {
                   // left or right
                   if (deltaX < 0) {
                       mSwipeDetected = Action.LR
                       return false
                   }
                   if (deltaX > 0) {
                       mSwipeDetected = Action.RL
                       return false
                   }
               } else

               // vertical swipe detection
               if (Math.abs(deltaY) > MIN_DISTANCE) {
                   // top or down
                   if (deltaY < 0) {
                       mSwipeDetected = Action.TB
                       return false
                   }
                   if (deltaY > 0) {
                       mSwipeDetected = Action.BT
                       return false
                   }
               }

               return false
           }

           MotionEvent.ACTION_DOWN -> {
               downX = event.x
               downY = event.y
               mSwipeDetected = Action.None
               var thisTime = System.currentTimeMillis()

               if (thisTime - lastTouchTime < 250) {
                   lastTouchTime = -1
               } else {
                   // too slow
                   lastTouchTime = thisTime
               }

               return false
           }
       }

        return false
    }
}