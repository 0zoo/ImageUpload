package xyz.e0zoo.todolistapplication.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import xyz.e0zoo.todolistapplication.R
import xyz.e0zoo.todolistapplication.api.authApi
import xyz.e0zoo.todolistapplication.api.model.SignInUserBody
import xyz.e0zoo.todolistapplication.api.updateToken
import xyz.e0zoo.todolistapplication.utils.enqueue

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        signInButton.setOnClickListener { _ ->
            val body = SignInUserBody("1ezoo94@gmail.com", "AAaa12345!")
            val call = authApi.getAccessToken(body)
            call.enqueue({ response ->
                response.body()?.let { auth ->

                    updateToken(this, auth.token)

                    toast("로그인 성공!")

                    startActivity<MainActivity>()
                }

            }, {
                toast(it.message.toString())
            })

        }

    }
}
