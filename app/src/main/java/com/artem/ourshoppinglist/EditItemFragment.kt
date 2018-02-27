package com.artem.ourshoppinglist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_edit_item.*
import kotlinx.android.synthetic.main.fragment_edit_item.view.*

class EditItemFragment : Fragment() {
    private var database = FirebaseDatabase.getInstance()
    private var activityCallback: ReplaceFragmentInterface? = null
    private var activityToolbarCallback: ChangeToolbarTitleInterface? = null
    private lateinit var listKey: String
    private lateinit var itemKey: String
    private var categoryNames = ArrayList<String>()
    private var categoryKeys = ArrayList<String>()
    private var arrOfCategoryNames = arrayOfNulls<String>(0)
    private lateinit var spinner: Spinner

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_edit_item, container, false)

        setHasOptionsMenu(true)
        activityToolbarCallback?.replaceToolbarTitle("")

        var barcode: String
        var category: String
        var itemName: String
        var quantity: Int

        if(arguments != null) {
            listKey = arguments.getString("listKey", "")
            itemKey = arguments.getString("key", "")
            barcode = arguments.getString("barcode", "")
            category = arguments.getString("category", "")
            itemName = arguments.getString("itemName", "")
            quantity = arguments.getInt("quantity", 0)

            view.fragment_edit_item_et_quantity_input.setText(quantity.toString())
            view.fragment_edit_item_et_barcode_input.setText(barcode)
            view.fragment_edit_item_et_item_name_input.setText(itemName)

            var spinnerPos = categoryNames.indexOf(category)

            if (spinnerPos >= 0 && spinnerPos < categoryNames.size) {
                view.fragment_edit_item_spnr_category_selection.setSelection(spinnerPos)
            }
        }

        spinner = view.fragment_edit_item_spnr_category_selection
        fillCategoryNamesAndKeys()

        //var spinnerCategoryAdapter = ArrayAdapter(context, R.layout.category_spinner_item, categoryNames)
        //spinnerCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //view.fragment_edit_item_spnr_category_selection.adapter = spinnerCategoryAdapter

        var list_of_items = arrayOf("Item 1", "Item 2", "Item 3")

        var arrOfItems =  arrayOfNulls<String>(categoryNames.size)
        for(i in arrOfItems.indices) {
            arrOfItems[i] = "stuff" + i
        }

        view.fragment_edit_item_btn_add_photo.setOnClickListener {
            addPhoto()
        }

        view.fragment_edit_item_btn_scan_barcode.setOnClickListener {
            scanBarcode()
        }

        view.fragment_edit_item_btn_search_barcode.setOnClickListener{
            searchWithBarcode(view.fragment_edit_item_et_barcode_input.text.toString())
        }

        return view
    }

    //Grabs all of Category names that are in the database, and their keys
    private fun fillCategoryNamesAndKeys(){
        //Grabs the categories from the DataSnapshot and adds them to the list
        var categoryListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                categoryNames.clear()
                categoryKeys.clear()

                for(postSnapShot in dataSnapshot!!.children) {
                    var category = postSnapShot.getValue(Category::class.java)
                    categoryNames.add(category!!.categoryName)
                    categoryKeys.add(category!!.key)
                }

                arrOfCategoryNames = arrayOfNulls<String>(categoryNames.size)

                for(i in arrOfCategoryNames.indices) {
                    arrOfCategoryNames[i] = categoryNames[i]
                }

                var spinnerCategoryAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, arrOfCategoryNames)
                spinnerCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                spinner.adapter = spinnerCategoryAdapter
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                println("loadPost:onCancelled ${databaseError!!.toException()}")
            }
        }

        //Grabs all current Categories that are part of a shopping list
        var categoriesRef = database.getReference("Categories")
        categoriesRef.orderByChild("belongsToListKey").equalTo(listKey).addListenerForSingleValueEvent(categoryListener)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.findItem(R.id.action_cancel)?.isVisible = true
        menu?.findItem(R.id.action_save)?.isVisible = true

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var selected =  super.onOptionsItemSelected(item)
        var id = item?.itemId

        when(id) {
            R.id.action_cancel -> {
                selected = true
                cancel()
            }

            R.id.action_save -> {
                selected = true
                saveItem()
            }
        }

        return selected
    }

    //Switches to a screen to allow the scanning of a barcode
    private fun scanBarcode(){
        //todo implement being able to scan a barcode using mobile vision
    }

    //Checks if there's an item with the existing barcode within the API call
    private fun searchWithBarcode(barcode: String){
        //todo implement searching for an item with a barcode
    }

    private fun addPhoto(){
        //todo implement adding a photo to an item
    }

    //Saves the current item, or creates a new item
    private fun saveItem(){
        var categoryNamePos = categoryNames.indexOf(fragment_edit_item_spnr_category_selection.selectedItem.toString())
        var categoryName: String
        var categoryKey: String
        var barcode: String
        var itemName: String
        var quantity: Int

        if(categoryNamePos >= 0 && categoryNamePos < categoryKeys.size){
            categoryKey = categoryKeys[categoryNamePos]
            categoryName = fragment_edit_item_spnr_category_selection.selectedItem.toString()
            itemName = fragment_edit_item_et_item_name_input.text.toString()
            quantity = fragment_edit_item_et_quantity_input.text.toString().toInt()
            barcode = fragment_edit_item_et_barcode_input.text.toString()

            var ref = database.getReference("CategoryItems")

            if(itemKey != null && itemKey != ""){
                //update the item with the current itemKey
                var category = CategoryItem(itemName, quantity, barcode, itemKey, categoryKey, categoryName, listKey)

                ref.child(itemKey).setValue(category)

                //todo add a toast for saying that it saved
            } else {
                //create a new item
                var createdCategoryItemRef = ref.push()
                var category = CategoryItem(itemName, quantity, barcode, createdCategoryItemRef.key, categoryKey, categoryName, listKey)

                createdCategoryItemRef.setValue(category)

                //todo add a toast for saying that it created the new item
            }
        }
    }

    //Goes back to the list of category items
    private fun cancel(){
        activity.onBackPressed()
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
}