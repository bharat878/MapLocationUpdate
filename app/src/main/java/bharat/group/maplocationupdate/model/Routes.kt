package bharat.group.maplocationupdate.model

data class Routes(
    var geocoded_waypoints: List<GeocodedWaypoint?>?,
    var routes: List<Route?>?,
    var status: String?
)