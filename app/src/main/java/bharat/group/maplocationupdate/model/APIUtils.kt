package bharat.group.maplocationupdate.model

import android.content.Context
import android.widget.Toast
import bharat.group.maplocationupdate.api.JsonPlaceHolderApi
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIUtils {


    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://96gw5cphgi.execute-api.ap-south-1.amazonaws.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val jsonPlaceHolderApi : JsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi::class.java)


    fun sendLocation(locationData: LocationData,context : Context){
        val locationCall = jsonPlaceHolderApi.addLocation(locationData)

        locationCall.enqueue(object  : Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Toast.makeText(context,"Location Updated",Toast.LENGTH_SHORT).show()
            }
        })

    }


}