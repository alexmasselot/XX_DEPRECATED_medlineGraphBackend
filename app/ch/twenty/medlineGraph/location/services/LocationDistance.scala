package ch.twenty.medlineGraph.location.services

import ch.twenty.medlineGraph.location.GeoCoordinates

/**
 * @author Alexandre Masselot.
 */
object LocationDistance {
  /**
   * get distance in meters from 2 coordinates
   * @param coord1 point 1
   * @param coord2 point 2
   * @return in meters
   */
  def distance(coord1: GeoCoordinates, coord2: GeoCoordinates): Double = {
    val earthRadius = 6371000
    val dLat = Math.toRadians(coord2.latitude - coord1.latitude)
    val dLng = Math.toRadians(coord2.longitude - coord1.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(Math.toRadians(coord1.latitude)) * Math.cos(Math.toRadians(coord2.latitude)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    earthRadius * c
  }
}
