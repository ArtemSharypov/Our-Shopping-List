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
import kotlinx.android.synthetic.main.dialog_edit_and_create_category.view.*
import kotlinx.android.synthetic.main.fragment_selected_list.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.ExpandableListView


class SelectedListFragment : Fragment(), SelectedListAdapter.EditListItemsInterface {
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
        var list = view.fragment_selected_list_elv_lists
        val swipeDetector = SwipeDetector()

        if(arguments != null){
            listKey = arguments.getString("key")
            listName = arguments.getString("listName")
        }

        setHasOptionsMenu(true)
        activityToolbarCallback?.replaceToolbarTitle(listName)

        setupCategoryListener()
        setupCategoryItemsListener()

        view.fragment_selected_list_fab_new_item.setOnClickListener {
            createNewCategoryItem()
        }

        selectedListAdapter = SelectedListAdapter(context, categoriesList, categoryItems, this)

        list.setAdapter(selectedListAdapter)

        list.setOnTouchListener(swipeDetector)

        list.onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val itemType = ExpandableListView.getPackedPositionType(id)

                if (swipeDetector.swipeDetected()) {
                    //onSwipeAction for only child items
                    if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        var childPosition = ExpandableListView.getPackedPositionChild(id)
                        var groupPosition = ExpandableListView.getPackedPositionGroup(id)

                        var groupItem = categoriesList[groupPosition]
                        var listOfChildItems = categoryItems[groupItem.categoryName]
                        var childItem = listOfChildItems?.get(childPosition)

                        //Removes the child item from the list display, as well as from the backend
                        if(listOfChildItems != null && childItem != null) {
                            listOfChildItems.remove(childItem)
                            selectedListAdapter.notifyDataSetChanged()

                            var ref = database.getReference("CategoryItems")
                            ref.child(childItem.key).removeValue()

                            reduceListItemsCountBy(-1)
                        }
                    }
                } else {
                    // do the onItemClick action
                }
            }
        }

        list.onItemLongClickListener = object : OnItemLongClickListener {
            override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
                val itemType = ExpandableListView.getPackedPositionType(id)

                if (swipeDetector.swipeDetected()) {
                    //onSwipeAction for only child items
                    if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        var childPosition = ExpandableListView.getPackedPositionChild(id)
                        var groupPosition = ExpandableListView.getPackedPositionGroup(id)

                        var groupItem = categoriesList[groupPosition]
                        var listOfChildItems = categoryItems[groupItem.categoryName]
                        var childItem = listOfChildItems?.get(childPosition)

                        //Removes the child item from the list display, as well as from the backend
                        if(listOfChildItems != null && childItem != null) {
                            listOfChildItems.remove(childItem)
                            selectedListAdapter.notifyDataSetChanged()

                            var ref = database.getReference("CategoryItems")
                            ref.child(childItem.key).removeValue()

                            reduceListItemsCountBy(-1)
                        }
                    }

                    return true
                } else {
                    // do the onItemLongClick action
                    return false
                }
            }
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

        if(categoryItemsList == null){
            categoryItemsList = ArrayList<CategoryItem>()
        }

        categoryItemsList.add(categoryItem)


        categoryItems[category] = categoryItemsList
    }

    //Creates a dialog for the name of a new category
    private fun createNewCategoryDialog(){
        //Create a dialog popup for creating a new category
        var inflater = activity.layoutInflater
        var promptsView = inflater.inflate(R.layout.dialog_edit_and_create_category, null)
        var alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptsView)

        var categoryNameInput = promptsView.dialog_edit_and_create_category_et_category_input

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
        menu?.findItem(R.id.action_create_category)?.isVisible = true

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

            R.id.action_create_category -> {
                selected = true
                createNewCategoryDialog()
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
        var ref = database.getReference("Lists")
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
    override fun categoryItemEditClicked(categoryItem: CategoryItem) {
        var editItemFragment = EditItemFragment()

        var bundle = Bundle()
        bundle.putString("key", categoryItem.key)
        bundle.putString("listKey", categoryItem.belongsToListKey)
        bundle.putString("barcode", categoryItem.barcode)
        bundle.putString("category", categoryItem.categoryName)
        bundle.putString("itemName", categoryItem.itemName)
        bundle.putInt("quantity", categoryItem.quantity)

        editItemFragment.arguments = bundle

        activityCallback?.replaceFragment(editItemFragment)
    }

    //Creates a dialog for editing, or deleting a category
    override fun categoryEditClicked(category: Category){
        var inflater = activity.layoutInflater
        var promptsView = inflater.inflate(R.layout.dialog_edit_and_create_category, null)
        var alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptsView)

        var categoryNameInput = promptsView.dialog_edit_and_create_category_et_category_input
       categoryNameInput.setText(category.categoryName)

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("Save", {dialogInterface, i ->
                    category.categoryName = categoryNameInput.text.toString()

                    var ref = database.getReference("Categories")
                    ref.child(category.key).setValue(category)

                    selectedListAdapter.notifyDataSetInvalidated()

                    dialogInterface.cancel()
                })
                .setNegativeButton("Cancel", {dialogInterface, i ->
                    dialogInterface.cancel()
                })
                .setNeutralButton("Delete") {dialogInterface, i ->
                    confirmDeleteCategoryDialog(category)
                }

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    //Creates a dialog for confirmation on whether or not to delete the category
    //If the category is to be deleted, then it removes it and all categoryItems that belong to it from the list/database
    private fun confirmDeleteCategoryDialog(category: Category) {
        var inflater = activity.layoutInflater
        var promptsView = inflater.inflate(R.layout.dialog_delete_category_confirmation, null)
        var alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptsView)

        alertDialogBuilder.setCancelable(true)
                .setPositiveButton("Delete", {dialogInterface, i ->
                    var ref = database.getReference("Categories")
                    ref.child(category.key).removeValue()

                    deleteAllCategoryItemsUnderCategory(category)

                    categoriesList.remove(category)
                    categoryItems.remove(category.categoryName)

                    selectedListAdapter.notifyDataSetInvalidated()

                    dialogInterface.cancel()
                })
                .setNegativeButton("Cancel", {dialogInterface, i ->
                    dialogInterface.cancel()
                })
        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    //Deletes all categoryItems from Firebase that have the same key as the category passed in
    private fun deleteAllCategoryItemsUnderCategory(category: Category) {
        var ref = database.getReference("CategoryItems")
        var numItemsDeleted = 0

        //Grabs the category items from the DataSnapshot and adds them to the map
        var categoryItemsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                for(postSnapShot in dataSnapshot!!.children) {
                    var categoryItem = postSnapShot.getValue(CategoryItem::class.java)
                    ref.child(categoryItem?.key).removeValue()
                    numItemsDeleted--
                }

                reduceListItemsCountBy(numItemsDeleted)
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                println("loadPost:onCancelled ${databaseError!!.toException()}")
            }
        }

        var categoryItemsRef = database.getReference("CategoryItems")
        categoryItemsRef.orderByChild("belongsToCategoryKey").equalTo(category.key).addListenerForSingleValueEvent(categoryItemsListener)
    }

    //Updates the numItems field for a ShoppingList in the back end for when CategoryItem's / Category's are deleted/removed
    private fun reduceListItemsCountBy(count: Int) {
        var listsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                var list = dataSnapshot?.getValue(ShoppingList::class.java)

                if(list?.numItems != null) {
                    list.numItems = list?.numItems + count

                    //Updates the list with the new count
                    var ref = database.getReference("Lists")
                    ref.child(listKey).setValue(list)
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                println("loadPost:onCancelled ${databaseError!!.toException()}")
            }
        }

        var ref = database.getReference("Lists")
        ref.child(listKey).addListenerForSingleValueEvent(listsListener)
    }
}