package com.artem.ourshoppinglist

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signedin.*

class SignedinActivity : AppCompatActivity(), ReplaceFragmentInterface, ChangeToolbarTitleInterface {

    private var fbAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signedin)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        fbAuth.addAuthStateListener {
            if(fbAuth.currentUser == null){
                //Clear the activity stack, and then sets up for exiting the activity
                var intent = Intent(applicationContext, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra("EXIT", true)
                startActivity(intent)
            }
        }

        var signedinHomeFragment = SignedinHomeFragment()

        supportFragmentManager.beginTransaction()
                .add(R.id.activity_signedin_frame_fragment_holder, signedinHomeFragment)
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

    override fun replaceToolbarTitle(newTitle: String) {
        supportActionBar?.title = newTitle
    }

    //Signs out the current user from Firebase
    private fun signOut(){
        fbAuth.signOut()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1)
        {
            supportFragmentManager.popBackStack()
        }
        else
        {
            var promptsView = layoutInflater.inflate(R.layout.dialog_exit_app_confirmation, null)
            var alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setView(promptsView)

            alertDialogBuilder.setCancelable(true)
                    .setPositiveButton("Yes", {dialogInterface, i ->
                        //Clear the activity stack, and then sets up for exiting the activity
                        var intent = Intent(applicationContext, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra("EXIT", true)
                        startActivity(intent)

                        dialogInterface.cancel()
                    })
                    .setNegativeButton("No", {dialogInterface, i ->
                        dialogInterface.cancel()
                    })

            var alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }
}
