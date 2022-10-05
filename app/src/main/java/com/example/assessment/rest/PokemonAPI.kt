package com.example.assessment.rest

import com.example.assessment.rest.model.QueryResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PokemonAPI {

    @GET("pokemon")
    fun queryPokemons(@Query("/") query: Int): Call<QueryResult?>
}