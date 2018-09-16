package br.ufpe.cin.if710.rss

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Intent
import android.net.Uri

// Variavel global que sera usada no onclick do item da lista
var listaItens: List<ItemRSS>? = null

class ItemAdapter (private val itens: List<ItemRSS>?): RecyclerView.Adapter<ItemAdapter.ViewHolderItem>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderItem {

        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        // Associa o Holder com o xml da linha "ïtemlista.xml"
        val view: View = layoutInflater.inflate(R.layout.itemlista, parent, false)
        // Seta o valor para a variavel global
        listaItens = itens

        return ViewHolderItem(view, parent.context)
    }

    // Cria o bind para cada item da lista enviada para a classe
    override fun onBindViewHolder(holder: ViewHolderItem, position: Int) {
        val item: ItemRSS = itens!![position]
        holder.bindModel(item)
    }

    // Retorna a quantidade de itens da lista
    override fun getItemCount(): Int {
        if (itens != null) return itens.size
        return 0
    }


    class ViewHolderItem(row: View, val context: Context): RecyclerView.ViewHolder(row), View.OnClickListener {
        var titulo: TextView? = null
        var data: TextView? = null

        init {
            titulo = row.findViewById(R.id.item_titulo)
            data = row.findViewById(R.id.item_data)
            row.setOnClickListener(this)
        }

        fun bindModel(i: ItemRSS?){
            // Associa os componentes do layout itemlista com os valores do itemRSS
            titulo?.text = i?.title
            data?.text = i?.pubDate

        }

        override fun onClick(v: View?) {
            // Abre o browser a partir do click em algum item da lista
            val position = adapterPosition
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(listaItens!![position].link))
            context.startActivity(browserIntent)
        }

    }


}

