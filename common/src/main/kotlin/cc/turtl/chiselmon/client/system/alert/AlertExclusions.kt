package cc.turtl.chiselmon.client.system.alert

/** World-scoped locations where alerts are disabled. */
data class AlertExclusions(
    val zones: MutableList<AlertExclusionZone> = mutableListOf(),
) {
    fun contains(
        dimensionNamespace: String,
        dimensionPath: String,
        x: Int,
        y: Int,
        z: Int,
    ): Boolean = zones.any { it.contains(dimensionNamespace, dimensionPath, x, y, z) }
}

/** A spherical alert exclusion zone centered on a block position. */
data class AlertExclusionZone(
    val dimensionNamespace: String,
    val dimensionPath: String,
    var x: Int,
    var y: Int,
    var z: Int,
    var radius: Int = DEFAULT_RADIUS,
) {
    val dimension: String get() = "$dimensionNamespace:$dimensionPath"

    val coordinates: String get() = "$x, $y, $z"

    fun updateCoordinates(value: String): Boolean {
        val parts = value.split(',')
        if (parts.size != 3) return false

        val x = parts[0].trim().toIntOrNull() ?: return false
        val y = parts[1].trim().toIntOrNull() ?: return false
        val z = parts[2].trim().toIntOrNull() ?: return false
        this.x = x
        this.y = y
        this.z = z
        return true
    }

    fun contains(
        dimensionNamespace: String,
        dimensionPath: String,
        x: Int,
        y: Int,
        z: Int,
    ): Boolean {
        if (this.dimensionNamespace != dimensionNamespace || this.dimensionPath != dimensionPath) return false

        val dx = x.toLong() - this.x
        val dy = y.toLong() - this.y
        val dz = z.toLong() - this.z
        val radius = radius.toLong()
        return dx * dx + dy * dy + dz * dz <= radius * radius
    }

    companion object {
        const val DEFAULT_RADIUS = 32
    }
}
