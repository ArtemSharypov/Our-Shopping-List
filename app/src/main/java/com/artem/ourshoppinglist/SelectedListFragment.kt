package com.artem.ourshoppinglist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signedin.*
import kotlinx.android.synthetic.main.dialog_create_new_category.view.*
import kotlinx.android.synthetic.main.fragment_selected_list.*

class SelectedListFragment : Fragment(), SelectedListAdapter.EditCategoryItem {
    private var fbAuth = FirebaseAuth.getInstance()
    private var activityCallback: ReplaceFragmentInterface? = null
    private lateinit var selectedListAdapter: SelectedListAdapter
    private lateinit var listKey: String
    private lateinit var listName: String

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_selected_list, container, false)

        if(arguments != null){
            listKey = arguments.getString("key")
            listName = arguments.getString("listName")
        }

        setHasOptionsMenu(true)
        activity_signedin_tv_toolbar_title.text = listName

        //todo get all categories and items for a list from firebase and populate the adapter with it
        var categoriesList = ArrayList<Category>()
        var categoryItems = HashMap<String, ArrayList<CategoryItem>>()

        selectedListAdapter = SelectedListAdapter(context, categoriesList, categoryItems, this)

        fragment_selected_list_elv_lists.setAdapter(selectedListAdapter)

        fragment_selected_list_fab_new_item.setOnClickListener {
            createNewCategoryItem()
        }

        fragment_selected_list_btn_new_category.setOnClickListener {
            createNewCategoryDialog()
        }

        //todo add listeners to firebase for when items are added/removed in categories, and categoryItems

        return view
    }

    //Creates a dialog for the name of a new category
    private fun createNewCategoryDialog(){
        //Create a dialog popup for creating a new category
        var inflater = activity.layoutInflater
        var promptsView = inflater.inflate(R.layout.dialog_create_new_category, null)
        var alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptsView)

        var categoryNameInput = promptsView.dialog_create_new_category_et_category_input

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("Create Category", {dialogInterface, i ->
                    //todo create the category in firebase

                    dialogInterface.cancel()

                })
                .setNegativeButton("Cancel", {dialogInterface, i ->
                    dialogInterface.cancel()
                })

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_signedin, menu)

        menu?.findItem(R.id.action_delete)?.isVisible = true
        menu?.findItem(R.id.action_back)?.isVisible = true

        super.onCreateOptionsMenu(menu, inflater)
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
                activityCallback?.replaceFragment(settingsFragment)
            }

            R.id.action_delete -> {
                selected = true
                deleteList()
            }

            R.id.action_back -> {
                selected = true
                goBack()
            }
        }

        return selected
    }

    //Signs out the current user from Firebase
    private fun signOut(){
        fbAuth.signOut()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            activityCallback = context as ReplaceFragmentInterface
        } catch (e: ClassCastException) {
            throw ClassCastException(context?.toString() + " must implement ReplaceFragmentInterface")
        }
    }

    //Goes back to the previous screen
    private fun goBack(){
        activity.onBackPressed()
    }

    //Deletes the current list
    private fun deleteList() {
        //todo implement deleting a list
    }

    //Switches to create a new category item fragment
    private fun createNewCategoryItem(){
        var editItemFragment = EditItemFragment()

        var bundle = Bundle()
        bundle.putString("listKey", listKey)

        editItemFragment.arguments = bundle

        activityCallback?.replaceFragment(editItemFragment)
    }

    //Switches to editing a category item fragment
    override fun categoryItemEditClicked(categoryItem: CategoryItem, listKey: String) {
        var editItemFragment = EditItemFragment()

        var bundle = Bundle()
        bundle.putString("key", categoryItem.key)
        bundle.putString("listKey", listKey)
        bundle.putString("barcode", categoryItem.barcode)
        bundle.putString("category", categoryItem.categoryName)
        bundle.putString("itemName", categoryItem.itemName)
        bundle.putInt("quantity", categoryItem.quantity)

        editItemFragment.arguments = bundle

        activityCallback?.replaceFragment(editItemFragment)
    }
}