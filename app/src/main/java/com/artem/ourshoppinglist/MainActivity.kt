package com.artem.ourshoppinglist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment

class MainActivity : AppCompatActivity(), SigninFragment.ReplaceFragmentInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        if (fragmentManager.backStackEntryCount> 0)
        {
            fragmentManager.popBackStack()
        } else
        {
            super.onBackPressed()
        }
    }
}
