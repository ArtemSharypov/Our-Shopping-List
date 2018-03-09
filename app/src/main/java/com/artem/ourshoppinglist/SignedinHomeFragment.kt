package com.artem.ourshoppinglist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.dialog_create_list.view.*
import kotlinx.android.synthetic.main.fragment_signedin_home.view.*

class SignedinHomeFragment : Fragment(), ShoppingListAdapter.OnListClicked {
    private var fbAuth = FirebaseAuth.getInstance()
    private var database = FirebaseDatabase.getInstance()
    private var activityCallback: ReplaceFragmentInterface? = null
    private var activityToolbarCallback: ChangeToolbarTitleInterface? = null
    private var shoppingLists = ArrayList<ShoppingList>()
    private var adapter: ShoppingListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_signedin_home, container, false)

        setHasOptionsMenu(true)
        activityToolbarCallback?.replaceToolbarTitle(getString(R.string.app_name))

        setupListsListener()

        adapter = ShoppingListAdapter(shoppingLists, this)

        view.fragment_signedin_home_rv_lists.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        view.fragment_signedin_home_rv_lists.adapter = adapter

        view.fragment_signedin_home_fab_create_list.setOnClickListener{
            createNewListDialog()

        }

        return view
    }

    //Grabs all of the ShoppingList's that belong to the current signed in user, and sets up a listener for them
    private fun setupListsListener(){
        //todo check if theres an issue with push interaction and reading a new item being added in between devices?
        //Grabs the lists from the DataSnapshot and adds them to the shoppingLists
        var listsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                shoppingLists.clear()

                for(postSnapShot in dataSnapshot!!.children) {
                    var list = postSnapShot.getValue(ShoppingList::class.java)

                    if(list?.belongsToUser == fbAuth.currentUser?.uid){
                        shoppingLists.add(list!!)
                    }
                }

                if(adapter != null) {
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                println("loadPost:onCancelled ${databaseError!!.toException()}")
            }
        }

        //Grabs all current Lists that belong to the current user
        var ref = database.getReference("Lists")
        ref.orderByChild("belongsToUser").equalTo(fbAuth.currentUser?.uid).addListenerForSingleValueEvent(listsListener)

        //Listens for data being added/removed, and adds or removes lists as necessary
        ref = database.getReference("Lists")
        ref.addValueEventListener(listsListener)
    }

    //Creates a dialog for creating a new list
    private fun createNewListDialog(){
        //Create a dialog popup for creating a new list
        var inflater = activity.layoutInflater
        var promptsView = inflater.inflate(R.layout.dialog_create_list, null)
        var alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptsView)

        var listNameInput = promptsView.dialog_create_list_tv_title_et_list_name

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("Create List", {dialogInterface, i ->
                    var ref = database.getReference("Lists")
                    var createdListsRef = ref.push()
                    var shoppingList = ShoppingList(createdListsRef.key, listNameInput.text.toString(), 0, fbAuth.currentUser?.uid!!)
                    createdListsRef.setValue(shoppingList)

                    var selectedListFragment = SelectedListFragment()

                    var bundle = Bundle()
                    bundle.putString("key", shoppingList.key)
                    bundle.putString("listName", shoppingList.listName)

                    selectedListFragment.arguments = bundle

                    activityCallback?.replaceFragment(selectedListFragment)

                    dialogInterface.cancel()
                })
                .setNegativeButton("Cancel", {dialogInterface, i ->
                    dialogInterface.cancel()
                })

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            activityCallback = context as ReplaceFragmentInterface
            activityToolbarCallback = context as ChangeToolbarTitleInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(context?.toString() + " must implement ReplaceFragmentInterface")
        }
    }

    //Switches to the list that was clicked
    override fun shoppingListClicked(shoppingList: ShoppingList) {
        var selectedListFragment = SelectedListFragment()

        var bundle = Bundle()
        bundle.putString("key", shoppingList.key)
        bundle.putString("listName", shoppingList.listName)

        selectedListFragment.arguments = bundle

        activityCallback?.replaceFragment(selectedListFragment)
    }
}