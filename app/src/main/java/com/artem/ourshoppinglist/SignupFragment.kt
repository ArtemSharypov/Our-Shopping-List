package com.artem.ourshoppinglist

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.fragment_signup.view.*


class SignupFragment : Fragment() {
    private var fbAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_signup, container, false)

        view.fragment_signup_btn_create_account.setOnClickListener{
            signUp()
        }

        view.fragment_signup_tv_sign_in.setOnClickListener {
            switchToLogin()
        }

        return view
    }

    //Attempts to create a new user from the email and password entered
    private fun signUp() {
        //Tries to sign a user up, catch is meant for when password or email is an empty string to prevent a crash
        try {
            fbAuth.createUserWithEmailAndPassword(fragment_signup_et_email_input.text.toString(), fragment_signup_et_password.text.toString())
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            var intent = Intent(activity, SignedinActivity::class.java)
                            intent.putExtra("id", fbAuth.currentUser?.email)
                            startActivity(intent)
                        } else {
                            Snackbar.make(fragment_signup_btn_create_account, "Error: ${task.exception?.message}", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Action", null).show()
                        }
                    }
        } catch (exception: IllegalArgumentException) {
            Snackbar.make(fragment_signup_btn_create_account, "Error: Email or Password is empty", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show()
        }
    }

    //Returns back to the LoginActivity
    private fun switchToLogin() {
        activity.onBackPressed()
    }
}