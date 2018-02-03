package com.artem.ourshoppinglist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_signedin.*

class SignedinActivity : AppCompatActivity(), ReplaceFragmentInterface {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signedin)

        setSupportActionBar(toolbar)

        var signedinHomeFragment = SignedinHomeFragment()

        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_frame_fragment_holder, signedinHomeFragment)
                .addToBackStack(null)
                .commit()
    }

    override fun replaceFragment(fragment: Fragment) {
        if(fragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_signedin_frame_fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    override fun onBackPressed() {
        if (fragmentManager.backStackEntryCount> 0)
        {
            fragmentManager.popBackStack()
        }
        else
        {
            super.onBackPressed()
        }
    }
}
