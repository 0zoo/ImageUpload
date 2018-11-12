package xyz.e0zoo.todolistapplication.api

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import xyz.e0zoo.todolistapplication.api.model.Auth
import xyz.e0zoo.todolistapplication.api.model.Image
import xyz.e0zoo.todolistapplication.api.model.SignInUserBody


interface Service {

    @POST("auth/login")
    fun getAccessToken(@Body user: SignInUserBody): Call<Auth>

    @POST("images")
    fun postImage(): Call<Image>

    @PUT
    fun putImage(@Url url: String, @Body body: RequestBody): Call<Void>


}