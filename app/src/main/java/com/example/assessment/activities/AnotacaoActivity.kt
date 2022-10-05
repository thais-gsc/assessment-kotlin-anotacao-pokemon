package com.example.assessment.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.example.assessment.AnotacaoDAO
import com.example.assessment.Cripto
import com.example.assessment.InfoAnotacao
import com.example.assessment.R
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AnotacaoActivity : AppCompatActivity() {
    private var imgBArray: ByteArray? = null
    private var lat: String = ""
    private var lon: String = ""
    private val anotacaoDAO = AnotacaoDAO()
    val REQUEST_PERMISSIONS_CODE = 666
    val REQUEST_CAPTURE_IMAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anotacao)

        val imgAnotacao = this.findViewById<ImageView>(R.id.imgAnotacao)
        imgAnotacao.setOnClickListener{

            val pictureIntent = Intent (MediaStore.ACTION_IMAGE_CAPTURE)

            if (pictureIntent.resolveActivity(packageManager)!= null){
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == REQUEST_CAPTURE_IMAGE &&
            resultCode == Activity.RESULT_OK
        ) {
            if (data != null && data.extras != null) {
                val imgAnotacao = this.findViewById<ImageView>(R.id.imgAnotacao)
                val imageBitmap = data.extras!!["data"] as Bitmap?
                imgAnotacao.setImageBitmap(imageBitmap)

                val streamOutput = ByteArrayOutputStream()

                imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, streamOutput)
                val byteArray = streamOutput.toByteArray()
                imgBArray = byteArray
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun obterCoordenadas() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )
        val isNetworkEnabled = locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
        if (!isGPSEnabled && !isNetworkEnabled) {
            Log.d("Permissao", "Ative os serviços necessários")
        } else {
            if (isGPSEnabled) {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        30000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Log.d("Permissao", "Erro de permissão")
                }
            } else if (isNetworkEnabled) {
                try {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        2000L, 0f, locationListener
                    )
                } catch (ex: SecurityException) {
                    Log.d("Permissao", "Erro de permissão")
                }
            }
        }
    }

    val local = this.findViewById<TextView>(R.id.previewLocalizacao)
    private val locationListener: LocationListener =
        object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lat = "${location.latitude}"
                lon = "${location.longitude}"
                local.text = "$lat $lon"

            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

    fun salvarAnotacao(view: View?) {
        val permissionAFL = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionACL = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissionAFL != PackageManager.PERMISSION_GRANTED &&
            permissionACL != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                callDialog(
                    "É preciso permitir acesso à localização!",
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_CODE
                )
            }
        } else {
            obterCoordenadas()
            previewAnotacaoCripto()
        }
    }

    fun previewAnotacaoCripto(){
        val txtTituloAnotacao = this.findViewById<TextView>(R.id.txtTituloAnotacao)
        val titulo = txtTituloAnotacao.text.toString()
        val imgAnotacao = this.findViewById<ImageView>(R.id.imgAnotacao)
        val txtAnotacao = this.findViewById<TextView>(R.id.txtAnotacao)
        val anotacao = txtAnotacao.text.toString()

        if (!txtTituloAnotacao.text.isNullOrEmpty() && imgAnotacao != null && !txtAnotacao.text.isNullOrEmpty()) {
            val txtPreview = this.findViewById<TextView>(R.id.txtPreview)
            val previewCard = this.findViewById<CardView>(R.id.previewCard)
            val previewTitulo = this.findViewById<TextView>(R.id.previewTituloCard)
            val previewAnotacao = this.findViewById<TextView>(R.id.previewAnotacao)
            val previewImg = this.findViewById<ImageView>(R.id.previewImagem)
            val previewData = this.findViewById<TextView>(R.id.previewDataCard)

            txtPreview.visibility = View.VISIBLE
            previewCard.visibility = View.VISIBLE
            previewTitulo.text = titulo
            previewAnotacao.text =  anotacao
            previewImg.setImageBitmap(imgAnotacao.drawToBitmap())

            val hoje = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            val date = formatter.format(hoje)
            previewData.text = date

            val dataInsert = SimpleDateFormat("dd/MM/yyyy").format(Date())
            val data = previewData.setText(date)
            val nomeFile = "${txtTituloAnotacao.text.toString().uppercase(Locale.ROOT)}*${dataInsert}*"
            val nomeTxt = "$nomeFile.txt"
            val nomeImg = "$nomeFile.fig"
            Cripto().criptoGravarTxt(
                nomeTxt,
                this,
                listOf(lat, lon, anotacao)
            )
            Cripto().criptoGravarImg(
                nomeImg,
                this,
                imgBArray!!
            )
        } else {
            Toast.makeText(
                this,
                "Preencha todos os campos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun callDialog(
        mensagem: String,
        permissions: Array<String>
    ) {
        val mDialog = AlertDialog.Builder(this)
            .setTitle("Permissão")
            .setMessage(mensagem)
            .setPositiveButton("Ok")
            { dialog, id ->
                ActivityCompat.requestPermissions(
                    this@AnotacaoActivity, permissions,
                    REQUEST_PERMISSIONS_CODE
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancela")
            { dialog, id ->
                dialog.dismiss()
            }
        mDialog.show()
    }

    fun editarAnotacao(anotacao: InfoAnotacao) {
        anotacaoDAO.editar(anotacao)
    }

    fun retornarActvity(view: View) {
        finish()
    }
}