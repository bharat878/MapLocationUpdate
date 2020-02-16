package bharat.group.maplocationupdate.model

import android.content.Context
import android.util.Log
import android.widget.Toast
import bharat.group.maplocationupdate.api.JsonPlaceHolderApi
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.collections.set

class APIUtils {


    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://96gw5cphgi.execute-api.ap-south-1.amazonaws.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val jsonPlaceHolderApi: JsonPlaceHolderApi =
        retrofit.create(JsonPlaceHolderApi::class.java)


    fun sendLocation(locationData: LocationData, context: Context) {
        val locationCall = jsonPlaceHolderApi.addLocation(locationData)

        locationCall.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Toast.makeText(context, "Location Updated", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun getPolyGonPoints(
        originLatLng: LatLng,
        destinationLatLng: LatLng,
        onPolyLineListener: OnPolyLineListener
    ) {
        val baseUrlMap = "https://maps.googleapis.com/maps/api/directions/json"
        val hashMap = HashMap<String, String>()
        hashMap["origin"] = "${originLatLng.latitude},${originLatLng.longitude}"
        hashMap["destination"] = "${destinationLatLng.latitude},${destinationLatLng.longitude}"
        hashMap["sensor"] = "false"
        hashMap["key"] = "AIzaSyALgJEuPQy6H_YQTIORGIx1gAOHYulmxLw"

        val polyGonCall = jsonPlaceHolderApi.getPolygonPoints(baseUrlMap, hashMap)
        polyGonCall.enqueue(object : Callback<Routes> {
            override fun onFailure(call: Call<Routes>, t: Throwable) {
                Log.d("POLY",t.localizedMessage)
            }

            override fun onResponse(call: Call<Routes>, response: Response<Routes>) {
                if (response.isSuccessful) {
                    onPolyLineListener.onSuccess(decodeAndGetPolyLine(response.body()!!))
                }
            }
        })

    }

    fun decodeAndGetPolyLine(routes: Routes): List<LatLng> {
        val listOfPloyGons = mutableListOf<LatLng>()
        routes.routes?.forEach { eachRoute ->
            eachRoute?.legs?.forEach { eachLeg ->
                eachLeg?.steps?.forEach { eachStep ->
                   eachStep?.polyline?.points?.let { ppomt ->
                       val listOfLatLong = decodePoly(ppomt)
                       listOfLatLong?.let {
                           listOfPloyGons.addAll(it.toMutableList())
                       }
                   }

                }

            }
        }

        return listOfPloyGons
    }

    private fun decodePoly(encoded: String): List<LatLng>? {
        val poly: MutableList<LatLng> = ArrayList()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f) shl shift
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f) shl shift
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }
        return poly
    }

    interface OnPolyLineListener {
        fun onSuccess(list: List<LatLng>)
    }


}