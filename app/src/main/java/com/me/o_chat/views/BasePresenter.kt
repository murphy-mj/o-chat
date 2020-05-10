package com.me.o_chat.views

import android.content.Intent
import com.me.o_chat.views.BaseView
import com.me.o_chat.main.MainActivity

open class BasePresenter(var view: BaseView?) {

  var app: MainActivity =  view?.application as MainActivity

  open fun doActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
  }

  open fun doRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
  }

  open fun onDestroy() {
    view = null
  }
}