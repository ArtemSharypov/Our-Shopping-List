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

class SigninFragment : Fragment() {

    interface ReplaceFragmentInterface {
        fun replaceFragment(fragment: Fragment)
    }

    private var activityCallback: SigninFragment.ReplaceFragmentInterface? = null
    private var fbAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_signin, container, false)

        fragment_signin_btn_sign_in.setOnClickListener{
            signin()
        }

        fragment_signin_tv_create_account.setOnClickListener {
            switchToSignUp()
        }

        return view
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

        fbAuth.signInWithEmailAndPassword(fragment_signin_et_email_input.text.toString(), fragment_signin_et_password.text.toString())
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        fragment_signin_et_email_input.setText("")
                        fragment_signin_et_password.setText("")
                        snackBar.dismiss()

                        var intent = Intent(activity, SignedinActivity::class.java)
                        intent.putExtra("id", fbAuth.currentUser?.email)
                        startActivity(intent)

                    } else {
                        Snackbar.make(fragment_signin_btn_sign_in, "Error: ${task.exception?.message}", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", null).show()
                    }
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