package bharat.group.maplocationupdate.model

data class GeocodedWaypoint(
    var geocoder_status: String?,
    var place_id: String?,
    var types: List<String?>?
)