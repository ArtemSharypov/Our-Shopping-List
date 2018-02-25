package com.artem.ourshoppinglist

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_edit_item.*
import kotlinx.android.synthetic.main.fragment_edit_item.view.*

class EditItemFragment : Fragment() {
    private var activityCallback: ReplaceFragmentInterface? = null
    private var activityToolbarCallback: ChangeToolbarTitleInterface? = null
    private lateinit var listKey: String
    private lateinit var itemKey: String
    private var categoryNames = ArrayList<String>()
    private var categoryKeys = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_edit_item, container, false)

        listKey = ""
        itemKey = ""

        setHasOptionsMenu(true)
        activityToolbarCallback?.replaceToolbarTitle("")

        //todo get all categoryNames for a list from firebase
        //then parse each category retrieving name, as well as key


        var arrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, categoryNames)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        view.fragment_edit_item_spnr_category_selection.adapter = arrayAdapter

        var barcode: String
        var category: String
        var itemName: String
        var quantity: Int

        if(arguments != null) {
            listKey = arguments.getString("listKey")
            itemKey = arguments.getString("key", "")
            barcode = arguments.getString("barcode")
            category = arguments.getString("category")
            itemName = arguments.getString("itemName")
            quantity = arguments.getInt("quantity")

            view.fragment_edit_item_et_quantity_input.setText(quantity)
            view.fragment_edit_item_et_barcode_input.setText(barcode)
            view.fragment_edit_item_et_item_name_input.setText(itemName)

            var spinnerPos = categoryNames.indexOf(category)

            if (spinnerPos >= 0 && spinnerPos < categoryNames.size) {
                view.fragment_edit_item_spnr_category_selection.setSelection(spinnerPos)
            }
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
        //todo implement creating and updating an item in firebase

        var categoryNamePos = categoryNames.indexOf(fragment_edit_item_spnr_category_selection.selectedItem.toString())
        var categoryKey: String

        if(categoryNamePos >= 0 && categoryNamePos < categoryKeys.size){
            categoryKey = categoryKeys[categoryNamePos]

            if(itemKey != null && itemKey != ""){
                //update the item with the current itemKey
            } else {
                //create a new item
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