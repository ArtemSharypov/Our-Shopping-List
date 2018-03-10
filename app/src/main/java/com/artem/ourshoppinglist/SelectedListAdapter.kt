package com.artem.ourshoppinglist

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.row_category_item.view.*
import kotlinx.android.synthetic.main.row_category_title.view.*


class SelectedListAdapter(context: Context, categoriesList: ArrayList<Category>, categoryItems: HashMap<String, ArrayList<CategoryItem>>,
                          callback: SelectedListAdapter.EditListItemsInterface): BaseExpandableListAdapter(){
    private var context = context
    private var categoriesList = categoriesList
    private var categoryItems = categoryItems
    private var callback = callback

    interface EditListItemsInterface {
        fun categoryItemEditClicked(categoryItem: CategoryItem)
        fun categoryEditClicked(category: Category)
    }

    override fun getGroup(groupPosition: Int): Any {
       return categoriesList[groupPosition]
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, viewGroup: ViewGroup?): View {
        var category = getGroup(groupPosition) as Category
        var view: View

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.row_category_title, null)
        } else {
            view = convertView
        }

        if (isExpanded)
        {
            view.row_category_title_iv_arrow_dropdown.setImageResource(R.drawable.ic_expand_less_white_24dp)
        } else {
            view.row_category_title_iv_arrow_dropdown.setImageResource(R.drawable.ic_expand_more_white_24dp)
        }


        view.row_category_title_tv_category_name.text = category.categoryName
//        view.row_category_title_btn_edit_category.setOnClickListener {
//            callback.categoryEditClicked(category)
//        }

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isExpanded: Boolean, convertView: View?, viewGroup: ViewGroup?): View {
        var categoryItem = getChild(groupPosition, childPosition) as CategoryItem
        var view: View

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.row_category_item, null)
        } else {
            view = convertView
        }

        view.row_category_item_tv_item_name.text = categoryItem.itemName
        view.row_category_item_tv_item_quantity.text = categoryItem.quantity.toString()
        view.row_category_item_btn_edit_item.setOnClickListener {
            callback.categoryItemEditClicked(categoryItem)
        }

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        var categoryName = categoriesList[groupPosition].categoryName
        var count = 0

        if(categoryItems[categoryName] != null) {
            count = categoryItems[categoryName]!!.size
        }

        return count
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        var categoryName = categoriesList[groupPosition].categoryName

        return categoryItems[categoryName]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return categoriesList.size
    }

}