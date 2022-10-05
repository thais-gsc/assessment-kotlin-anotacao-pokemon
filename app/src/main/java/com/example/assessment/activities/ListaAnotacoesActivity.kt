package com.example.assessment.activities

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.example.assessment.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListaAnotacoesActivity : AppCompatActivity(),
    BillingClientStateListener,
    SkuDetailsResponseListener,
    PurchasesUpdatedListener,
    RecyclerViewItemListener {
    private lateinit var clienteInApp: BillingClient
    lateinit var auth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    private var currentSku = "android.test.purchased"
    private val PREF_FILE = "PREF_FILE"
    private var mapSku = HashMap<String, SkuDetails>()
    private lateinit var mAdView: AdView
    private val anotacaoDAO = AnotacaoDAO()
    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_anotacoes)

        auth = FirebaseAuth.getInstance()

        MobileAds.initialize(this)
        mAdView = this.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val btnLogout = this.findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            mUser = null
            finish()
        }

        val btnNovaAnotacao = this.findViewById<Button>(R.id.btnNovaAnotacao)
        btnNovaAnotacao.setOnClickListener {
            startActivity(Intent(this, AnotacaoActivity::class.java))
        }

        clienteInApp = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        clienteInApp.startConnection(this)

        checaCompra()
    }

    override fun onDestroy() {
        clienteInApp.endConnection()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mUser = auth.currentUser
        updateUI()
        criarRecycler()
    }

    fun criarRecycler() {
        val recycler = this.findViewById<RecyclerView>(R.id.rcyListaAnotacao)
        recycler.adapter = ListaAnotacoesAdapter()
        recycler.layoutManager =
            LinearLayoutManager(this)
    }

//    fun attRcy(): List<InfoAnotacao> {
//        val caminhoArquivo = File(this.filesDir.toURI())
//        var prefixo = ""
//        val dados = mutableListOf<InfoAnotacao>()
//        val files = caminhoArquivo.listFiles()
//
//        files?.forEach {
//            if ("$prefixo.txt" != it.name && "$prefixo.fig" != it.name) {
//                prefixo = it.name.removeSuffix(".txt")
//                prefixo = prefixo.removeSuffix(".fig")
//
//                dados.add(getDados(prefixo))
//            }
//        }
//        return dados
//    }

    private fun atualizarLista() {
        anotacaoDAO.mostrarTodas("email").addOnSuccessListener { listaDeAnotacoes ->
            val itens = ArrayList<InfoAnotacao>()
            for (nota in listaDeAnotacoes) {
                val anotacao = nota.toObject(InfoAnotacao::class.java)
                itens.add(anotacao)
            }

            val lista = this.findViewById<RecyclerView>(R.id.rcyListaAnotacao)
            lista.layoutManager = LinearLayoutManager(this)
            val adapter = ListaAnotacoesAdapter()
            adapter.listaAnotacoes = itens
            adapter.setRecyclerViewItemListener(this)
            lista.adapter = adapter
        }.addOnFailureListener { exception ->
            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
        }
    }

        override fun recyclerViewItemClicked(view: View, id: String) {
            TODO("Not yet implemented")
        }

    override fun deletarItem(view: View, id: String) {
        anotacaoDAO.deletar(id).addOnSuccessListener {
            atualizarLista()
            Toast.makeText(
                context,
                "Anotação deletada!",
                Toast.LENGTH_LONG
            ).show()
        }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Erro ao deletar",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun editarItem(view: View, id: String) {
        TODO("Not yet implemented")
    }

    fun inserirAnotacao(anotacao: InfoAnotacao) {
            anotacaoDAO.inserir(anotacao)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Anotação criada",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Erro ao criar anotação",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
        }

        fun getDados(prefixo: String): InfoAnotacao {
            val id = java.util.UUID.randomUUID().toString()
            var removeSuffix = prefixo.removeSuffix(".fig")
            removeSuffix = removeSuffix.removeSuffix(".txt")
            val imagem: ByteArray = Cripto().criptoLerImg("$removeSuffix.fig", this)

            val text: String = Cripto().criptoLerTxt("$removeSuffix.txt", this)[2]
            val tituloDado = prefixo.split("*")[0]
            val data = prefixo.split("*")[1].removeSuffix("*")
            val bitmap = BitmapFactory.decodeByteArray(imagem, 0, imagem.size)

            inserirAnotacao(InfoAnotacao(id, tituloDado, text, data, bitmap))

            return InfoAnotacao(
                id,
                tituloDado,
                text,
                data,
                bitmap
            )
        }

        fun updateUI() {
            val txtUsuario = this.findViewById<TextView>(R.id.txtUsuario)
            txtUsuario.text = "USUÁRIO:${mUser!!.email}"

        }

        override fun onBillingServiceDisconnected() {
            Log.d("COMPRA>>", "Serviço InApp desconectado")
        }

        override fun onBillingSetupFinished(p0: BillingResult) {
            if (p0.responseCode ==
                BillingClient.BillingResponseCode.OK
            ) {
                Log.d("COMPRA>>", "Serviço InApp conectado")
                val skuList = arrayListOf(currentSku)
                val params = SkuDetailsParams.newBuilder()
                params.setSkusList(skuList).setType(
                    BillingClient.SkuType.INAPP
                )
                clienteInApp.querySkuDetailsAsync(params.build(), this)
            }
        }

        override fun onSkuDetailsResponse(p0: BillingResult, p1: MutableList<SkuDetails>?) {
            if (p0.responseCode ==
                BillingClient.BillingResponseCode.OK
            ) {
                mapSku.clear()
                p1?.forEach { t ->
                    mapSku[t.sku] = t
                    val preco = t.price
                    val descricao = t.description
                    Log.d(
                        "COMPRA>>",
                        "Produto Disponível ($preco): $descricao"
                    )
                }
            }
        }

        override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
            if (p0.responseCode ==
                BillingClient.BillingResponseCode.OK &&
                p1 != null
            ) {

                for (purchase in p1) {
                    GlobalScope.launch(Dispatchers.IO) {
                        handlePurchase(purchase)
                    }
                }
            } else if (p0.responseCode ==
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
            ) {
                Log.d(
                    "COMPRA JÁ REALIZADA>>",
                    "Produto já foi comprado"
                )

                val userId = auth.currentUser?.uid
                val editor =
                    getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
                editor.putBoolean(userId, true)
                editor.apply()

            } else if (p0.responseCode ==
                BillingClient.BillingResponseCode.USER_CANCELED
            ) {
                Log.d(
                    "COMPRA CANCELADA>>",
                    "Usuário cancelou a compra"
                )

            } else {
                Log.d(
                    "ERRO NA COMPRA>>",
                    "Código de erro desconhecido: ${p0.responseCode}"
                )
            }
        }

        suspend fun handlePurchase(purchase: Purchase) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                Log.d(
                    "COMPRA>>",
                    "Produto obtido com sucesso, reinicie o app para remover os anúncios"
                )
                val userId = auth.currentUser?.uid
                val editor =
                    getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).edit()
                editor.putBoolean(userId, true)
                editor.apply()

                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams
                        .newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                    val ackPurchaseResult = withContext(Dispatchers.IO) {
                        clienteInApp.acknowledgePurchase(
                            acknowledgePurchaseParams.build()
                        )
                    }
                }
            }
        }


        fun checaCompra() {
            val adView = this.findViewById<AdView>(R.id.adView)
            val btnRemoveAd = this.findViewById<Button>(R.id.btnRemoveAd)

            val preferences =
                getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            val userId = auth.currentUser?.uid
            val isPurchase = preferences.getBoolean(userId, false)
            if (isPurchase) {
                adView.setVisibility(View.GONE)
                btnRemoveAd.setVisibility(View.GONE)
            }
        }
    }
