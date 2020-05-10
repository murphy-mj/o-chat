package com.me.o_chat.views.map

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.AnkoLogger
//import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread
import java.util.*
import com.me.o_chat.models.Station
import com.me.o_chat.views.BasePresenter
import com.me.o_chat.views.BaseView
import org.jetbrains.anko.async

class PlacemarkMapPresenter(view: BaseView) : BasePresenter(view), AnkoLogger {




  fun doPopulateMap(map: GoogleMap, stations: List<Station>) {
    map.uiSettings.setZoomControlsEnabled(true)
    stations.forEach {
      info("In Map Presenter pop map loop  ${it.sLocation.lat}")
      val loc  = LatLng(it.sLocation.lat, it.sLocation.lng)
      val options = MarkerOptions().title(it.sName).position(loc)
      map.addMarker(options).tag = it
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, it.sLocation.zoom))
    }
  }

  fun doMarkerSelected(marker: Marker) {
    //val tag = marker.tag as Long
    async {
      val station = marker.getTag() as Station
      info("In Marker Selected  ${station.sName}")
      uiThread {
        if (station != null) view?.showPlacemark(station)
      }
    }
  }

  fun loadPlacemarks() {
    async {
      val events = app.pObj.findAll()
      uiThread {
        info("load hillforts : ${events.size}")
       view?.showPlacemarks(events)
      }
    }
  }


}
