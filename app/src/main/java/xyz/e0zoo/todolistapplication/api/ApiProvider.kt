package xyz.e0zoo.todolistapplication.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val httpClient: OkHttpClient = OkHttpClient.Builder().apply {
    addInterceptor(loggingInterceptor)
}.build()


val authApi: Service = Retrofit.Builder().apply {
    baseUrl("http://10.0.2.2:3000/")
    client(httpClient)
    addConverterFactory(GsonConverterFactory.create())
}.build().create(Service::class.java)

fun provideApi(context: Context) = Retrofit.Builder().apply {
    baseUrl("http://10.0.2.2:3000/")
    client(authHttpClient(context))
    addConverterFactory(GsonConverterFactory.create())
}.build().create(Service::class.java)!!


class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val request = original.newBuilder().apply {

            getAccessToken(context)?.let { token ->
                addHeader("Authorization", "bearer $token")
            }

        }.build()

        return chain.proceed(request)
    }
}


fun authHttpClient(context: Context) = OkHttpClient.Builder().apply {
    addInterceptor(loggingInterceptor)
    addInterceptor(AuthInterceptor(context))
}.build()!!





