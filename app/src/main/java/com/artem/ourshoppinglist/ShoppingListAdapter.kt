package com.artem.ourshoppinglist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.row_shopping_list.view.*


class ShoppingListAdapter(shoppingLists: ArrayList<ShoppingList>, callback: ShoppingListAdapter.OnListClicked) : RecyclerView.Adapter<ShoppingListAdapter.ViewHolder>() {
    private var shoppingLists = shoppingLists
    private var callback = callback

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bindItems(currList: ShoppingList, callback: OnListClicked){
            itemView.row_shopping_list_tv_list_name.text = currList.listName
            itemView.row_shopping_list_tv_num_items_in_list.text = currList.listName
            itemView.setOnClickListener {
                callback.shoppingListClicked(currList)
            }
        }
    }

    interface OnListClicked {
        fun shoppingListClicked(shoppingList: ShoppingList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_shopping_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindItems(shoppingLists[position], callback)
    }

    override fun getItemCount(): Int {
        return shoppingLists.size
    }
}