package com.artem.ourshoppinglist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment

class MainActivity : AppCompatActivity(), ReplaceFragmentInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Exit the app if necessary
        if (intent.getBooleanExtra("EXIT", false)) {
            finish()
        }

        var signinFragment = SigninFragment()

        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_frame_fragment_holder, signinFragment)
                .addToBackStack(null)
                .commit()
    }

    override fun replaceFragment(fragment: Fragment){
        if(fragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_main_frame_fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0)
        {
            supportFragmentManager.popBackStack()
        }
        else
        {
            super.onBackPressed()
        }
    }
}
