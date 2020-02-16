package bharat.group.maplocationupdate.api

import bharat.group.maplocationupdate.model.LocationData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface JsonPlaceHolderApi {

    @Headers("Content-Type: application/json")
    @POST("latest")
   fun addLocation(@Body locationData: LocationData ) : Call<Void>
}