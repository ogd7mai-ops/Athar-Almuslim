package com.example.util

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import java.util.Locale

object LocationHelper {

    fun getCityAndCountryName(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale("ar"))
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val city = address.locality
                    ?: address.subAdminArea
                    ?: address.adminArea
                    ?: address.subLocality
                    ?: ""
                val country = address.countryName ?: ""

                if (city.isNotBlank() && country.isNotBlank()) {
                    "$city، $country"
                } else if (city.isNotBlank()) {
                    city
                } else if (country.isNotBlank()) {
                    country
                } else {
                    "مكة المكرمة، المملكة العربية السعودية"
                }
            } else {
                "مكة المكرمة، المملكة العربية السعودية"
            }
        } catch (e: Exception) {
            "مكة المكرمة، المملكة العربية السعودية"
        }
    }

    fun getCurrentDeviceLocation(context: Context): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            var bestLocation: Location? = null
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                @Suppress("MissingPermission")
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            bestLocation
        } catch (e: Exception) {
            null
        }
    }
}
