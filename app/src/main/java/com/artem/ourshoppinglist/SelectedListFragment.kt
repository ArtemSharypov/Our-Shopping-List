package com.artem.ourshoppinglist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.dialog_create_new_category.view.*
import kotlinx.android.synthetic.main.fragment_selected_list.view.*

class SelectedListFragment : Fragment(), SelectedListAdapter.EditCategoryItem {
    private var database = FirebaseDatabase.getInstance()
    private var activityCallback: ReplaceFragmentInterface? = null
    private var activityToolbarCallback: ChangeToolbarTitleInterface? = null
    private lateinit var selectedListAdapter: SelectedListAdapter
    private lateinit var listKey: String
    private lateinit var listName: String
    private var categoriesList = ArrayList<Category>()
    private var categoryItems = HashMap<String, ArrayList<CategoryItem>>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_selected_list, container, false)

        if(arguments != null){
            listKey = arguments.getString("key")
            listName = arguments.getString("listName")
        }

        setHasOptionsMenu(true)
        activityToolbarCallback?.replaceToolbarTitle(listName)

        setupCategoryListener()
        setupCategoryItemsListener()

        selectedListAdapter = SelectedListAdapter(context, categoriesList, categoryItems, this)

        view.fragment_selected_list_elv_lists.setAdapter(selectedListAdapter)

        view.fragment_selected_list_fab_new_item.setOnClickListener {
            createNewCategoryItem()
        }

        view.fragment_selected_list_btn_new_category.setOnClickListener {
            createNewCategoryDialog()
        }

        return view
    }

    //Grabs all current Categories for the current ShoppingList, and sets up a listener for changes
    private fun setupCategoryListener(){
        //Grabs the lists from the DataSnapshot and adds them to the shoppingLists
        var categoryListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                categoriesList.clear()

                for(postSnapShot in dataSnapshot!!.children) {
                    var category = postSnapShot.getValue(Category::class.java)

                    if(category?.belongsToListKey == listKey){
                        categoriesList.add(category!!)
                    }
                }

                if(selectedListAdapter != null) {
                    selectedListAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                println("loadPost:onCancelled ${databaseError!!.toException()}")
            }
        }

        //Grabs all current categories that belong to the current list
        var categoriesRef = database.getReference("Categories")
        categoriesRef.orderByChild("belongsToListKey").equalTo(listKey).addListenerForSingleValueEvent(categoryListener)

        //Listens for data being added/removed, and adds or removes lists as necessary
        categoriesRef = database.getReference("Categories")
        categoriesRef.addValueEventListener(categoryListener)
    }

    //Grabs all current CategoryItems for the current ShoppingList / Categories, and sets up a listener for changes
    private fun setupCategoryItemsListener(){
        //Grabs the category items from the DataSnapshot and adds them to the map
        var categoryItemsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                categoryItems.clear()

                for(postSnapShot in dataSnapshot!!.children) {
                    var categoryItem = postSnapShot.getValue(CategoryItem::class.java)

                    for(category in categoriesList) {
                        if(categoryItem?.belongsToCategoryKey == category.key) {
                            addCategoryItemToCategoryMap(categoryItem, category.categoryName)
                        }
                    }
                }

                if(selectedListAdapter != null) {
                    selectedListAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                println("loadPost:onCancelled ${databaseError!!.toException()}")
            }
        }

        //Grabs all current category items that are part of the current shopping list
        var categoryItemsRef = database.getReference("CategoryItems")
        categoryItemsRef.orderByChild("belongsToListKey").equalTo(listKey).addListenerForSingleValueEvent(categoryItemsListener)

        //Listens for data being added/removed, and adds or removes lists as necessary
        categoryItemsRef = database.getReference("CategoryItems")
        categoryItemsRef.addValueEventListener(categoryItemsListener)
    }

    //Adds a CategoryItem to the ArrayList of the Map containing all CategoryItem's belonging to their Category's
    private fun addCategoryItemToCategoryMap(categoryItem: CategoryItem, category: String) {
        var categoryItemsList = categoryItems[category]

        if(categoryItems == null){
            categoryItemsList = ArrayList<CategoryItem>()
        }

        categoryItemsList?.add(categoryItem)

        categoryItems[category] = categoryItemsList!!
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
                    var ref = database.getReference("Categories")
                    var createdListsRef = ref.push()
                    var category = Category(categoryNameInput.text.toString(), createdListsRef.key, listKey)
                    createdListsRef.setValue(category)

                    categoriesList.add(category)
                    selectedListAdapter.notifyDataSetInvalidated()

                    dialogInterface.cancel()
                })
                .setNegativeButton("Cancel", {dialogInterface, i ->
                    dialogInterface.cancel()
                })

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.findItem(R.id.action_delete)?.isVisible = true
        menu?.findItem(R.id.action_back)?.isVisible = true

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var selected =  super.onOptionsItemSelected(item)
        var id = item?.itemId

        when(id) {
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            activityCallback = context as ReplaceFragmentInterface
            activityToolbarCallback = context as ChangeToolbarTitleInterface
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
        var ref = database.getReference("Categories")
        ref.child(listKey).removeValue()

        goBack()
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