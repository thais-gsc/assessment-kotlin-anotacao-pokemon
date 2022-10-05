package com.example.assessment.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.assessment.R
import com.example.assessment.rest.PokemonService
import com.example.assessment.rest.PokemonServiceListener
import com.example.assessment.rest.model.QueryItem
import com.example.assessment.rest.model.QueryResult
import com.squareup.picasso.Picasso


class PokemonActivity : AppCompatActivity(), PokemonServiceListener {

    private val pokemonService = PokemonService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokemon)

        pokemonService.setPokemonServiceListener(this)
        pokemonService.queryPokemons(1)

        val nomePokemon = this.findViewById<TextView>(R.id.lblNomePokemon)

        nomePokemon.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ListaAnotacoesActivity::class.java)
            )
        }
    }

    override fun onResponse(queryResult: QueryResult?) {
        val nomePokemon = this.findViewById<TextView>(R.id.lblNomePokemon)
        val sprite = this.findViewById<ImageView>(R.id.imgPokemonAPI)

        if (queryResult != null) {
            val lstQueryItems = queryResult.items
            Log.i("DR3", "resultado com ${lstQueryItems.size} itens")

            val pokemonList = ArrayList<QueryItem>()
            for (queryItem in lstQueryItems) {
                Log.i("DR3"," ${queryItem.pokemonInfo.name}")
                nomePokemon.text = queryItem.pokemonInfo.name
                Picasso.with(this)
                    .load(queryItem.pokemonInfo.sprites.front_default)
                    .into(sprite)
            }
        }
    }

    override fun onFailure(message: String?) {
        Log.i("Dr3", "Erro = $message")
    }
}