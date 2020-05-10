package com.me.o_chat.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils.replace
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.me.o_chat.R
import com.me.o_chat.fragments.AboutUsFragment
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.activity_first.*

import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.home.*



class Home : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var ft: FragmentTransaction
    lateinit var User : User
  //  lateinit var app: MainApp


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
      //  app = application as MainApp
     //   if(FirebaseAuth.getInstance().currentUser != null) {
     //       User = app.pObj.findUserById(FirebaseAuth.getInstance().currentUser!!.uid.toString())!!
     //   }

        // this is for the Floating icon at the bottom of screen
      //  fab.setOnClickListener { view ->
      //      Snackbar.make(view, "Replace with your own action",
      //          Snackbar.LENGTH_LONG).setAction("Action", null).show()
      //  }

        navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        ft = supportFragmentManager.beginTransaction()

        val fragment = AboutUsFragment.newInstance()

        ft.replace(R.id.homeFrame, fragment)
        ft.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.nav_report ->
             //   navigateTo(ReportBasicFrag.newInstance())
                startActivity(Intent(this, NewStationActivity::class.java))
             //   navigateTo(PlacemarkFragment.newInstance())


            R.id.nav_aboutus ->
                navigateTo(AboutUsFragment.newInstance())

        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

 //   override fun onCreateOptionsMenu(menu: Menu): Boolean {
 //       menuInflater.inflate(R.menu.menu_home, menu)
 //       return true
 //   }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {

//        when (item.itemId) {
         //   R.id.action_donate -> toast("You Selected Donate")
         //   R.id.action_report -> toast("You Selected Report")
  //      }
//        return super.onOptionsItemSelected(item)
//    }

 //   override fun onBackPressed() {
 // /      if (drawerLayout.isDrawerOpen(GravityCompat.START))
 //           drawerLayout.closeDrawer(GravityCompat.START)
 //       else
 //           super.onBackPressed()
 //   }


  ///  fun StatsF() :Fragment {
     //   info("In Stats F I Stats F")
        //stats of Current User
     //   val args = Bundle()
     //   if(FirebaseAuth.getInstance().currentUser != null) {
     //          User = app.pObj.findUserById(FirebaseAuth.getInstance().currentUser!!.uid.toString())!!
     //   } else {
     //       User = UserModel("zzzz","Dummy","User")
     //   }

     //   args.putParcelable("User",User )
        //args.putString("selectedUser",user.Uuid )
    //    var frag = StatsFragment.newInstance()
    //    frag.arguments=args
    //    return frag
   // }


    //fun ImagesF() :Fragment {
        //stats of Current User
      //  val args = Bundle()
        //   if(FirebaseAuth.getInstance().currentUser != null) {
        //          User = app.pObj.findUserById(FirebaseAuth.getInstance().currentUser!!.uid.toString())!!
        //   } else {
        //       User = UserModel("zzzz","Dummy","User")
        //   }

        //   args.putParcelable("User",User )
        //args.putString("selectedUser",user.Uuid )
      //  var frag = StatsFragment.newInstance()
      //  frag.arguments=args
      //  return frag
  //  }






    private fun navigateTo(fragment: Fragment) {
       supportFragmentManager.beginTransaction()
           .replace(R.id.homeFrame, fragment)
            .addToBackStack("Home")
            .commit()
    }



}
