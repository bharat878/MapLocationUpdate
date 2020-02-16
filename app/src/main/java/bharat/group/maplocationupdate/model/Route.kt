package bharat.group.maplocationupdate.model

data class Route(
    var bounds: Bounds?,
    var copyrights: String?,
    var legs: List<Leg?>?,
    var overview_polyline: OverviewPolyline?,
    var summary: String?,
    var warnings: List<Any?>?,
    var waypoint_order: List<Any?>?
)