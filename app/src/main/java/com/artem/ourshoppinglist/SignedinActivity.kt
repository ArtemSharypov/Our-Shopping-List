package com.artem.ourshoppinglist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signedin.*

class SignedinActivity : AppCompatActivity(), ReplaceFragmentInterface {

    private var fbAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signedin)

        setSupportActionBar(toolbar)
        activity_signedin_tv_toolbar_title.text = getString(R.string.app_name)

        fbAuth.addAuthStateListener {
            if(fbAuth.currentUser == null){
                this.finish()
            }
        }

        var signedinHomeFragment = SignedinHomeFragment()

        supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_frame_fragment_holder, signedinHomeFragment)
                .addToBackStack(null)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_signedin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var selected =  super.onOptionsItemSelected(item)
        var id = item?.itemId

        when(id) {
            R.id.action_sign_out -> {
                selected = true
                signOut()
            }

            R.id.action_settings -> {
                selected = true
                var settingsFragment = SettingsFragment()
                replaceFragment(settingsFragment)
            }
        }

        return selected
    }

    override fun replaceFragment(fragment: Fragment) {
        if(fragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.activity_signedin_frame_fragment_holder, fragment)
                    .addToBackStack(null)
                    .commit()
        }
    }

    //Signs out the current user from Firebase
    private fun signOut(){
        fbAuth.signOut()
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
