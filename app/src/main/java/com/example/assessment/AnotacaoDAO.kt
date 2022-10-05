package com.example.assessment

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class AnotacaoDAO {
    private val collection = FirebaseFirestore.getInstance().collection("anotacoes")

    fun inserir(anotacao: InfoAnotacao): Task<DocumentReference> {
        return collection.add(anotacao)
    }

    fun deletar(anotacao: String): Task<Void> {
        return collection.document(anotacao.id).delete()
    }

    fun editar(anotacao: InfoAnotacao): Task<Void> {
        return collection.document(anotacao.id).update("titulo",anotacao.titulo, "texto",anotacao.texto)
    }

    fun mostrarTodas(email: String): Task<QuerySnapshot> {
        return collection.whereEqualTo("email", email).get()
    }
}