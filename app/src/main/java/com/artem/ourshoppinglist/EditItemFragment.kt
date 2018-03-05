package com.artem.ourshoppinglist

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class EditItemFragment : Fragment() {

    inner class BarcodeLookUpTask : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()

            var snackBar = Snackbar.make(fragment_edit_item_btn_scan_barcode, "Searching for the barcode....", Snackbar.LENGTH_INDEFINITE)
                snackBar.setAction("Action", null)
                snackBar.setAction("Cancel", object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        barcodeLookupTask.cancel(true)
                        snackBar.dismiss()
                    }
                })
        }

        override fun doInBackground(vararg args: String?): String {
            var response = ""

            if(args.isNotEmpty()) {
                var barcode = args[0].toString()

                try {
                    var baseURL = "https://api.upcitemdb.com/prod/trial/lookup?upc=" + barcode
                    var url = URL(baseURL)

                    with(url.openConnection() as HttpURLConnection) {
                        requestMethod = "GET"

                        BufferedReader(InputStreamReader(inputStream)).use {
                            val responseSB = StringBuffer()

                            var inputLine = it.readLine()
                            while(inputLine != null) {
                                responseSB.append(inputLine)
                                inputLine = it.readLine()
                            }

                            response = responseSB.toString()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if(result != null) {
                try {
                    var resultsJSON = JSONObject(result)
                    var resultCode = resultsJSON.getString("code")

                    if(resultCode == "OK") {
                        var itemsJSON = JSONArray(resultsJSON.getJSONArray("items"))

                        if (itemsJSON.length() > 0) {
                            var firstItemObj = itemsJSON.getJSONObject(0)

                            //todo add a dialog for if a user wants to use the item name
                            fragment_edit_item_et_item_name_input.setText(firstItemObj.getString("title"))
                        }
                    } else {
                        Snackbar.make(fragment_edit_item_btn_scan_barcode, "Error: " + resultCode, Snackbar.LENGTH_INDEFINITE)
                                .setAction("Action", null).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private var database = FirebaseDatabase.getInstance()
    private var activityCallback: ReplaceFragmentInterface? = null
    private var activityToolbarCallback: ChangeToolbarTitleInterface? = null
    private lateinit var listKey: String
    private lateinit var itemKey: String
    private var categoryNames = ArrayList<String>()
    private var categoryKeys = ArrayList<String>()
    private var arrOfCategoryNames = arrayOfNulls<String>(0)
    private lateinit var spinner: Spinner
    private lateinit var barcodeLookupTask: AsyncTask<String, Void, String>

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

        view.fragment_edit_item_btn_add_photo.setOnClickListener {
            addPhoto()
        }

        view.fragment_edit_item_btn_scan_barcode.setOnClickListener {
            scanBarcode()
        }

        view.fragment_edit_item_btn_search_barcode.setOnClickListener{
            searchWithBarcode()
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
        menu?.findItem(R.id.action_delete)?.isVisible = true

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

            R.id.action_delete -> {
                selected = true
                deleteItem()
            }
        }

        return selected
    }

    //Switches to a screen to allow the scanning of a barcode
    private fun scanBarcode(){
        //todo implement being able to scan a barcode using mobile vision
    }

    //Checks if there's an item with the existing barcode within the API call
    private fun searchWithBarcode(){
        val BARCODE_LENGTH = 12
        var currBarcode = fragment_edit_item_et_barcode_input.text.toString()

        //Check the necessary length
        if(currBarcode.length == BARCODE_LENGTH) {
            barcodeLookupTask = BarcodeLookUpTask().execute(currBarcode)
        } else {
            Snackbar.make(fragment_edit_item_btn_scan_barcode, "Error: The barcode must contain 12 digits", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    private fun addPhoto(){
        //todo implement adding a photo to an item
    }

    //Deletes the current item, and returns to the list screen
    private fun deleteItem(){
        if(::itemKey.isInitialized && itemKey != "") {
            var ref = database.getReference("CategoryItems")
            ref.child(itemKey).removeValue()

            Snackbar.make(fragment_edit_item_constraint_layout, "Deleted the item", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show()

            cancel()
        }
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
            } else {
                //create a new item
                var createdCategoryItemRef = ref.push()
                var category = CategoryItem(itemName, quantity, barcode, createdCategoryItemRef.key, categoryKey, categoryName, listKey)

                createdCategoryItemRef.setValue(category)
                itemKey = createdCategoryItemRef.key
            }

            Snackbar.make(fragment_edit_item_constraint_layout, "Saved the item", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    //Goes back to the list of category items
    private fun cancel(){
        if(::barcodeLookupTask.isInitialized) {
            barcodeLookupTask.cancel(true)
        }

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