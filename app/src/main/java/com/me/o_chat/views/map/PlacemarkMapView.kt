package com.me.o_chat.views.map

import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.activity_placemark_map.*
import kotlinx.android.synthetic.main.content_placemark_maps.*
import com.me.o_chat.helpers.readImageFromPath
import com.me.o_chat.views.BaseView
import com.me.o_chat.views.map.PlacemarkMapPresenter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import com.me.o_chat.models.Location
import com.me.o_chat.R
import com.me.o_chat.models.Station


class PlacemarkMapView : BaseView(), GoogleMap.OnMarkerClickListener,AnkoLogger {

  lateinit var presenter: PlacemarkMapPresenter
  lateinit var map : GoogleMap


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_placemark_map)
    super.init(toolbar, true)

    presenter = initPresenter (PlacemarkMapPresenter(this)) as PlacemarkMapPresenter

    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync {
      map = it
      map.setOnMarkerClickListener(this)
      presenter.loadPlacemarks()
    }
  }

  override fun showPlacemark(station: Station) {
    info("In showPlacemark ??  ${station.sName}")
    currentTitle.text = station.sName
    currentDescription.text = station.sDescription
    Glide.with(this).load(station.sImage).into(currentImage)
  }

  override fun showPlacemarks(stations: List<Station>) {
    info("Showplacemarks Size : ${stations.size}")
    presenter.doPopulateMap(map, stations)
  }

  override fun onMarkerClick(marker: Marker): Boolean {
    info("In on Marker Click ${marker.title}")
    presenter.doMarkerSelected(marker)
    return true
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.onDestroy()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapView.onLowMemory()
  }

  override fun onPause() {
    super.onPause()
    mapView.onPause()
  }

  override fun onResume() {
    super.onResume()
    mapView.onResume()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mapView.onSaveInstanceState(outState)
  }
}
