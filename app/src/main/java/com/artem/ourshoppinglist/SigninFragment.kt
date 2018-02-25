package com.artem.ourshoppinglist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_signin.*
import kotlinx.android.synthetic.main.fragment_signin.view.*

class SigninFragment : Fragment() {
    private var activityCallback: ReplaceFragmentInterface? = null
    private var fbAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_signin, container, false)

        view.fragment_signin_btn_sign_in.setOnClickListener{
            signin()
        }

        view.fragment_signin_tv_create_account.setOnClickListener {
            switchToSignUp()
        }

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var user = fbAuth.currentUser

        if(user != null) {
            var intent = Intent(activity, SignedinActivity::class.java)
            startActivity(intent)
        }
    }

    //Tries to use the email and password provided to sign into firebase
    fun signin(){
        var snackBar = Snackbar.make(fragment_signin_btn_sign_in, "Authenticating...", Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction("Action", null)
        snackBar.setAction("dismiss", object : View.OnClickListener {
            override fun onClick(p0: View?) {
                snackBar.dismiss()
            }
        })
        snackBar.show()

        //Tries to sign a user in, catch is meant for when password or email is an empty string to prevent a crash
        try {
            fbAuth.signInWithEmailAndPassword(fragment_signin_et_email_input.text.toString(), fragment_signin_et_password.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            fragment_signin_et_email_input.setText("")
                            fragment_signin_et_password.setText("")
                            snackBar.dismiss()

                            var intent = Intent(activity, SignedinActivity::class.java)
                            startActivity(intent)

                        } else {
                            Snackbar.make(fragment_signin_btn_sign_in, "Error: ${task.exception?.message}", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Action", null).show()
                        }
                    }
        } catch (exception: IllegalArgumentException) {
            Snackbar.make(fragment_signin_btn_sign_in, "Error: Email or Password is empty", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            activityCallback = context as ReplaceFragmentInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(context?.toString() + " must implement ReplaceFragmentInterface")
        }
    }

    //Switches to the SignupFragment
    fun switchToSignUp(){
        var signupFragment = SignupFragment()
        activityCallback?.replaceFragment(signupFragment)
    }
}