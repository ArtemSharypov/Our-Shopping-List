package com.artem.ourshoppinglist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_create_list.view.*
import kotlinx.android.synthetic.main.fragment_signedin_home.*

class SignedinHomeFragment : Fragment(), ShoppingListAdapter.OnListClicked {
    private var fbAuth = FirebaseAuth.getInstance()
    private var activityCallback: ReplaceFragmentInterface? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_signedin_home, container, false)

        fbAuth.addAuthStateListener {
            if(fbAuth.currentUser == null){
                activity.finish()
            }
        }

        //todo get all list names and their keys here from firebase
        //then populate the list with it
        var shoppingLists = ArrayList<ShoppingList>()

        fragment_signedin_home_rv_lists.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        fragment_signedin_home_rv_lists.adapter = ShoppingListAdapter(shoppingLists, this)


        fragment_signedin_home_fab_create_list.setOnClickListener{
            //Create a dialog popup for creating a new list
            var inflater = activity.layoutInflater
            var promptsView = inflater.inflate(R.layout.dialog_create_list, null)
            var alertDialogBuilder = AlertDialog.Builder(activity)
            alertDialogBuilder.setView(promptsView)

            var listNameInput = promptsView.dialog_create_list_tv_title_et_list_name

            alertDialogBuilder.setCancelable(true)
                    .setPositiveButton("Create List", {dialogInterface, i ->
                        //todo create the list in firebase
                        //once created then switch to the fragment

                        var selectedListFragment = SelectedListFragment()

                        var bundle = Bundle()
                        bundle.putString("key", "key")
                        bundle.putString("listName", "listName")

                        activityCallback?.replaceFragment(selectedListFragment)
                    })
                    .setNegativeButton("Cancel", {dialogInterface, i ->
                        dialogInterface.cancel()
                    })

            var alertDialog = alertDialogBuilder.create()
            alertDialog.show()

        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            activityCallback = context as ReplaceFragmentInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(context?.toString() + " must implement ReplaceFragmentInterface")
        }
    }

    override fun shoppingListClicked(shoppingList: ShoppingList) {
        var selectedListFragment = SelectedListFragment()

        var bundle = Bundle()
        bundle.putString("key", shoppingList.key)
        bundle.putString("listName", shoppingList.listName)

        selectedListFragment.arguments = bundle

        activityCallback?.replaceFragment(selectedListFragment)
    }
}