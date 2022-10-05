package com.example.assessment.rest

import com.example.assessment.rest.model.QueryResult

interface PokemonServiceListener {
    fun onResponse(queryResult: QueryResult?)

    fun onFailure(message: String?)
}