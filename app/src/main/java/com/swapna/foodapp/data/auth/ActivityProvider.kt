package com.swapna.foodapp.data.auth

import android.app.Activity
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

// Holds a weak reference to the current Activity
// WeakReference prevents memory leaks
// MainActivity sets this in onResume, clears in onPause

@Singleton
class ActivityProvider @Inject constructor() {

    private var activityRef: WeakReference<Activity>? = null

    fun setActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun getActivity(): Activity? = activityRef?.get()

    fun clearActivity() {
        activityRef = null
    }
}