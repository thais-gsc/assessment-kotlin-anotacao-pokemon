package com.example.assessment

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assessment.activities.AnotacaoActivity

class ListaAnotacoesAdapter():
    RecyclerView.Adapter<ListaAnotacoesAdapter.MyViewHolder>() {

    var listaAnotacoes = listOf<InfoAnotacao>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    lateinit var itemListener: RecyclerViewItemListener

    fun setRecyclerViewItemListener(listener: RecyclerViewItemListener) {
        itemListener = listener
    }

    class MyViewHolder(view: View) :
        RecyclerView.ViewHolder(view){
        var titulo = view.findViewById<TextView>(R.id.recyclerTitulo)
        val anotacao = view.findViewById<TextView>(R.id.recyclerAnotacao)
        val data = view.findViewById<TextView>(R.id.recyclerData)
        val img = view.findViewById<ImageView>(R.id.recyclerImg)


        fun bindItem(anotacao: InfoAnotacao, itemListener: RecyclerViewItemListener, position: Int) {
            val titulo = itemView.findViewById<TextView>(R.id.recyclerTitulo)
            titulo.text = anotacao.titulo
            val textoAnotacao = itemView.findViewById<TextView>(R.id.recyclerAnotacao)
            textoAnotacao.text = anotacao.texto

            itemView.setOnClickListener {
                itemListener.recyclerViewItemClicked(it, anotacao.id)
            }

            val btnDeletar = itemView.findViewById<ImageButton>(R.id.btnDeletar)
            btnDeletar.setOnClickListener {
                itemListener.deletarItem(it, anotacao.id)
            }

            val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditar)
            btnEditar.setOnClickListener {
                itemListener.editarItem(it, anotacao.id)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.lista_anotacoes,
                parent,
                false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listaAnotacoes.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindItem(listaAnotacoes[position], itemListener, position)
    }


}