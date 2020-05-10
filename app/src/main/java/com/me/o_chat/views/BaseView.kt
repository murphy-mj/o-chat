package com.me.o_chat.views

import android.content.Intent

import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.AnkoLogger
import com.me.o_chat.models.Location
import com.me.o_chat.views.map.PlacemarkMapView
import com.me.o_chat.activities.Home
import com.me.o_chat.models.Station
import com.me.o_chat.views.BasePresenter


val IMAGE_REQUEST = 1
val LOCATION_REQUEST = 2

enum class VIEW {
  LOCATION, PLACEMARK, MAPS, LIST, LOGIN, HOME, NAVIGATOR, SEARCH
}

open abstract class BaseView() : AppCompatActivity(), AnkoLogger {

  var basePresenter: BasePresenter? = null


  fun navigateTo(view: VIEW, code: Int = 0, key: String = "", value: Parcelable? = null) {

    when (view) {

      VIEW.MAPS -> intent = Intent(this, PlacemarkMapView::class.java)
      VIEW.HOME -> intent = Intent(this, Home::class.java)
    //  VIEW.SEARCH -> intent = Intent(this, SearchHView::class.java)

    }
    if (key != "") {
      intent.putExtra(key, value)
    }
    startActivityForResult(intent, code)
  }

  fun initPresenter(presenter: BasePresenter): BasePresenter {
    basePresenter = presenter
    return presenter
  }

  fun init(toolbar: Toolbar, upEnabled: Boolean) {
    toolbar.title = title
    setSupportActionBar(toolbar)
    val user = FirebaseAuth.getInstance().currentUser
    if (user != null) {
      toolbar.title = "${title}: ${user.email}"
    }
    supportActionBar?.setDisplayHomeAsUpEnabled(upEnabled)
  }

  override fun onDestroy() {
    basePresenter?.onDestroy()
    super.onDestroy()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (data != null) {
      basePresenter?.doActivityResult(requestCode, resultCode, data)
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    basePresenter?.doRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  open fun showPlacemark(station: Station) {}
  open fun showPlacemarks(stations: List<Station>) {}
  open fun showLocation(location : Location) {}
  open fun showProgress() {}
  open fun hideProgress() {}
}