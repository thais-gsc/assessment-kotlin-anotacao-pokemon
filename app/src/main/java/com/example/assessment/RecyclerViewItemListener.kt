package com.example.assessment

import android.view.View

interface RecyclerViewItemListener {
    fun recyclerViewItemClicked(view: View, id: String)

    fun deletarItem(view: View, id: String)

    fun editarItem(view: View, id: String)
}