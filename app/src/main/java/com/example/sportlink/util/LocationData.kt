package com.example.sportlink.util

/**
 * Predefined locations for sports in Brașov, Romania.
 * Each sport has 3 public locations with coordinates for Google Maps.
 */

data class SportLocation(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

object LocationData {
    /**
     * Gets predefined locations for a sport in Brașov.
     * 
     * @param sportName The name of the sport
     * @return List of 3 predefined locations for the sport
     */
    fun getLocationsForSport(sportName: String): List<SportLocation> {
        return when (sportName) {
            "Fotbal" -> listOf(
                SportLocation(
                    name = "Teren Fotbal Parcul Central",
                    address = "Parcul Central, Brașov",
                    latitude = 45.6427,
                    longitude = 25.5887
                ),
                SportLocation(
                    name = "Teren Fotbal Stadionul Municipal",
                    address = "Stadionul Municipal, Brașov",
                    latitude = 45.6500,
                    longitude = 25.6000
                ),
                SportLocation(
                    name = "Teren Fotbal Noua",
                    address = "Cartierul Noua, Brașov",
                    latitude = 45.6350,
                    longitude = 25.5750
                )
            )
            "Tenis" -> listOf(
                SportLocation(
                    name = "Terenuri Tenis Parcul Central",
                    address = "Parcul Central, Brașov",
                    latitude = 45.6427,
                    longitude = 25.5887
                ),
                SportLocation(
                    name = "Club Tenis Brașov",
                    address = "Strada Republicii, Brașov",
                    latitude = 45.6400,
                    longitude = 25.5900
                ),
                SportLocation(
                    name = "Terenuri Tenis Tractorul",
                    address = "Cartierul Tractorul, Brașov",
                    latitude = 45.6300,
                    longitude = 25.5800
                )
            )
            "Baschet" -> listOf(
                SportLocation(
                    name = "Teren Baschet Parcul Central",
                    address = "Parcul Central, Brașov",
                    latitude = 45.6427,
                    longitude = 25.5887
                ),
                SportLocation(
                    name = "Sala Sporturilor Brașov",
                    address = "Strada Stadionului, Brașov",
                    latitude = 45.6500,
                    longitude = 25.6000
                ),
                SportLocation(
                    name = "Teren Baschet Noua",
                    address = "Cartierul Noua, Brașov",
                    latitude = 45.6350,
                    longitude = 25.5750
                )
            )
            "Volei" -> listOf(
                SportLocation(
                    name = "Teren Volei Parcul Central",
                    address = "Parcul Central, Brașov",
                    latitude = 45.6427,
                    longitude = 25.5887
                ),
                SportLocation(
                    name = "Sala Sporturilor Brașov",
                    address = "Strada Stadionului, Brașov",
                    latitude = 45.6500,
                    longitude = 25.6000
                ),
                SportLocation(
                    name = "Teren Volei Noua",
                    address = "Cartierul Noua, Brașov",
                    latitude = 45.6350,
                    longitude = 25.5750
                )
            )
            "Handbal" -> listOf(
                SportLocation(
                    name = "Sala Handbal Brașov",
                    address = "Strada Stadionului, Brașov",
                    latitude = 45.6500,
                    longitude = 25.6000
                ),
                SportLocation(
                    name = "Teren Handbal Parcul Central",
                    address = "Parcul Central, Brașov",
                    latitude = 45.6427,
                    longitude = 25.5887
                ),
                SportLocation(
                    name = "Sala Sporturilor Noua",
                    address = "Cartierul Noua, Brașov",
                    latitude = 45.6350,
                    longitude = 25.5750
                )
            )
            "Badminton" -> listOf(
                SportLocation(
                    name = "Sala Badminton Brașov",
                    address = "Strada Stadionului, Brașov",
                    latitude = 45.6500,
                    longitude = 25.6000
                ),
                SportLocation(
                    name = "Teren Badminton Parcul Central",
                    address = "Parcul Central, Brașov",
                    latitude = 45.6427,
                    longitude = 25.5887
                ),
                SportLocation(
                    name = "Club Badminton Noua",
                    address = "Cartierul Noua, Brașov",
                    latitude = 45.6350,
                    longitude = 25.5750
                )
            )
            else -> emptyList()
        }
    }
}

