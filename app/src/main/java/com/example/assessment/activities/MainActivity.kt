package com.example.assessment.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assessment.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        mUser = auth.currentUser

        if (mUser != null) {
            startActivity(
                Intent(
                    this,
                    PokemonActivity::class.java)
            )
        }

        val inputEmail: TextInputEditText? = this.findViewById(R.id.txtEmailCadastro)
        val inputSenha : TextInputEditText? = this.findViewById(R.id.txtSenhaCadastro)

        val btnCadastrar = this.findViewById<Button>(R.id.btnCadastrar)
        btnCadastrar.setOnClickListener {
            val inputEmail: TextInputEditText? = this.findViewById(R.id.txtEmailCadastro)
            val inputSenha : TextInputEditText? = this.findViewById(R.id.txtSenhaCadastro)
            try {
                cadastrar(inputEmail, inputSenha)
                    .addOnSuccessListener {
                        mUser = it.user
                        Toast.makeText(
                            this,
                            "Cadastro realizado com sucesso.",
                            Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            it.message,
                            Toast.LENGTH_LONG).show()
                    }
                Toast.makeText(
                    this,
                    "Efetuando Cadastro.",
                    Toast.LENGTH_LONG).show()
            } catch (e: Throwable) {
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG).show()
            }
        }

        val btnLogin = this.findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val inputEmail: TextInputEditText? = this.findViewById(R.id.txtEmailCadastro)
            val inputSenha : TextInputEditText? = this.findViewById(R.id.txtSenhaCadastro)
            try {
                fazerLogin(inputEmail, inputSenha)
                    .addOnSuccessListener {
                        if (it != null){
                            Toast.makeText(
                                this,
                                "Bem vindo ${it.user?.email}!",
                                Toast.LENGTH_LONG
                            ).show()
                            startActivity(
                                Intent(
                                    this,
                                    PokemonActivity::class.java
                                )
                            )
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this,
                            it.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } catch (e: Throwable){
                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val callbackManager = CallbackManager.Factory.create()
        val buttonFacebookLogin = this.findViewById<LoginButton>(R.id.login_button)

        buttonFacebookLogin.setReadPermissions("email", "public_profile")
        buttonFacebookLogin.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
        // ...

    }

    fun cadastrar(
        inputEmail: TextInputEditText? = this.findViewById(R.id.txtEmailCadastro),
        inputSenha : TextInputEditText? = this.findViewById(R.id.txtSenhaCadastro)
    )
            : Task<AuthResult> {
        val email = inputEmail?.text.toString()
        val senha = inputSenha?.text.toString()
        if (email.isEmpty()
            || senha.isEmpty())
            throw object : Throwable() {
                override val message: String
                    get() = "Todos os campos são obrigatórios"
            }
        return auth.createUserWithEmailAndPassword(
            email, senha
        )
    }

    fun fazerLogin(
        inputEmail: TextInputEditText? = this.findViewById(R.id.txtEmailCadastro),
        inputSenha : TextInputEditText? = this.findViewById(R.id.txtSenhaCadastro)
    )
            : Task<AuthResult> {
        val email = inputEmail?.text.toString()
        val senha = inputSenha?.text.toString()
        if (email.isEmpty() || senha.isEmpty())
            throw object : Throwable() {
                override val message: String
                    get() = "Todos os campos são obrigatórios"
            }
        return auth.signInWithEmailAndPassword(
            email, senha
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val callbackManager = CallbackManager.Factory.create()
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    startActivity(
                        Intent(
                            this,
                            PokemonActivity::class.java
                        )
                    )
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}