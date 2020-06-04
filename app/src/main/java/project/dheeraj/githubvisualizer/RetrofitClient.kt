package project.dheeraj.githubvisualizer

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient{

    lateinit var retrofit: Retrofit
    final var BASE_URL = AppConfig.GITHUB_BASE_URL

    var gson = GsonBuilder()
        .setLenient()
        .create()


    fun getClient() : Retrofit
    {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }


}