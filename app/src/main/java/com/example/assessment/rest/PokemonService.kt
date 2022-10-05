package com.example.assessment.rest

import com.example.assessment.activities.PokemonActivity
import com.example.assessment.rest.model.QueryResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokemonService {

    private var api: PokemonAPI
    private lateinit var listener: PokemonServiceListener

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(PokemonAPI::class.java)
    }

    fun setPokemonServiceListener(listener: PokemonServiceListener) {
        this.listener = listener
    }

    fun queryPokemons(numero: Int) {

        val call = api.queryPokemons(numero)

        call!!.enqueue(object : Callback<QueryResult?> {
            override fun onResponse(call: Call<QueryResult?>, response: Response<QueryResult?>) {
                if (response.isSuccessful) {
                    listener.onResponse(response.body())
                }
            }

            override fun onFailure(call: Call<QueryResult?>, t: Throwable) {
                listener.onFailure(t.message)
            }
        })
    }



}