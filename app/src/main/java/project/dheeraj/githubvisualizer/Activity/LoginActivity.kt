/*
 * MIT License
 *
 * Copyright (c) 2020 Dheeraj Kotwani
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package project.dheeraj.githubvisualizer.Activity

import GithubUserModel
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import project.dheeraj.githubvisualizer.*
import project.dheeraj.githubvisualizer.AppConfig.ACCESS_TOKEN
import project.dheeraj.githubvisualizer.AppConfig.NAME
import project.dheeraj.githubvisualizer.AppConfig.SHARED_PREF
import project.dheeraj.githubvisualizer.Network.GithubApiClient
import project.dheeraj.githubvisualizer.Network.GithubApiInterface
import project.dheeraj.githubvisualizer.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var icon:ImageView
    private lateinit var usernameEditText:TextInputEditText
    private lateinit var passwordEditText:TextInputEditText
//    private lateinit var loginButton:Button
    private lateinit var username:String
    private lateinit var password:String
    private lateinit var auth: FirebaseAuth
    private lateinit var githubAuthProvider: GithubAuthProvider
    private lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initialiseViews()
        if (auth.currentUser!=null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        loginButton.setOnClickListener(this)


    }

    private fun initialiseViews() {
        icon = findViewById(R.id.github_icon)
        usernameEditText = findViewById(R.id.et_username)
        passwordEditText = findViewById(R.id.et_password)
//        loginButton = findViewById(R.id.loginButton)
        auth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences(
            SHARED_PREF, Context.MODE_PRIVATE)

        Glide.with(this)
            .load(R.drawable.github_loader)
            .into(gitLoader)


    }

    override fun onClick(view: View) {
        when(view.id){
            R.id.loginButton->checkFields()

        }
    }

    private fun checkFields() {
        username = usernameEditText.text.toString()
        password = passwordEditText.text.toString()

        if (username.isNullOrBlank()){
            usernameEditText.error = "Username can't be empty"
            usernameEditText.requestFocus()
            return
        }
//        if (password.isNullOrBlank()){
//            passwordEditText.error = "Password can't be empty"
//            passwordEditText.requestFocus()
//            return
//        }

        verifyCredentials(username, password)

    }

    private fun verifyCredentials(username: String, password: String) {

        if (!loaderImage.isVisible)
            loaderImage.visibility = View.VISIBLE

        if (loaderImage.isClickable)
            loaderImage.isClickable = false

        loginButton.isClickable = false

        var token: String = Credentials.basic(username, password);
        val provider: OAuthProvider.Builder = OAuthProvider.newBuilder("github.com")
        provider.addCustomParameter("login", username)
        val scopes: ArrayList<String?> = object : ArrayList<String?>() {
            init {
                add(AppConfig.OAUTH2_SCOPE)
            }
        }
        provider.scopes = scopes

        auth
            .startActivityForSignInWithProvider( /* activity= */this, provider.build())
            .addOnSuccessListener {
                // User is signed in.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
                // authResult.getCredential().getAccessToken().
                Toast.makeText(
                    this,
                    "Welcome ${auth.currentUser!!.displayName}",
                    Toast.LENGTH_SHORT
                ).show()


                sharedPref.edit()
                    .putString(ACCESS_TOKEN, (it!!.credential as OAuthCredential).accessToken)
                    .putString(NAME, auth.currentUser!!.displayName)
                    .apply()




                val apiInterface =
                        GithubApiClient.getClient().create(GithubApiInterface::class.java);
                    var call: Call<GithubUserModel> =
                        apiInterface.getUserData("token ${(it!!.credential as OAuthCredential).accessToken}")
                    call.enqueue(object : Callback<GithubUserModel> {
                        override fun onFailure(call: Call<GithubUserModel>, t: Throwable) {
                            Toast.makeText(
                                this@LoginActivity,
                                "error: ${t.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            if (loaderImage.isVisible)
                                loaderImage.visibility = View.GONE


                            if (loaderImage.isFocused)
                                loaderImage.clearFocus()

                            loginButton.isClickable = true

                        }

                        override fun onResponse(call: Call<GithubUserModel>, response: Response<GithubUserModel>) {

                            if (loaderImage.isVisible)
                                loaderImage.visibility = View.GONE


                            if (loaderImage.isFocused)
                                loaderImage.clearFocus()

                            loginButton.isClickable = true

                            Log.d("RESPONSE", response.message())
                            Log.d("UserName", response.body()!!.login)

                            sharedPref.edit()
                                .putString(ACCESS_TOKEN, (it!!.credential as OAuthCredential).accessToken)
                                .putString(NAME, auth.currentUser!!.displayName)
                                .putString(AppConfig.LOGIN, response.body()!!.login)
                                .apply()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()

                        }
                    })

            }
            .addOnFailureListener {
                if (loaderImage.isFocused)
                    loaderImage.clearFocus()

                if (loaderImage.isVisible)
                    loaderImage.visibility = View.GONE

                loginButton.isClickable = true

                Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
            }

    }

}

