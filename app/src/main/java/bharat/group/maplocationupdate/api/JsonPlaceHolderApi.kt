package bharat.group.maplocationupdate.api

import bharat.group.maplocationupdate.model.LocationData
import bharat.group.maplocationupdate.model.Routes
import retrofit2.Call
import retrofit2.http.*

interface JsonPlaceHolderApi {

    @Headers("Content-Type: application/json")
    @POST("latest")
   fun addLocation(@Body locationData: LocationData ) : Call<Void>

    @GET
    fun getPolygonPoints(@Url url : String,@QueryMap queryMap: HashMap<String,String>) : Call<Routes>
}