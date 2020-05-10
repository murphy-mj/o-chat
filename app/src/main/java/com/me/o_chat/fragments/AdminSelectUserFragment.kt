package com.me.o_chat.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.me.hillfort.utils.SwipeToEditCallback

import com.me.o_chat.R
import com.me.o_chat.SwipeToDeleteCallback
import com.me.o_chat.UserAdapter
import com.me.o_chat.UserListener
import com.me.o_chat.models.User
import kotlinx.android.synthetic.main.fragment_basicreport.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.fragment_user.view.swiperefresh

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [AdminSelectUserFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [AdminSelectUserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AdminSelectUserFragment : Fragment(), UserListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    lateinit var root: View
    lateinit var selectedEvent : String


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }



    }


 //   override fun onAttach(context: Context?) {
 //       super.onAttach(context)
 //       if (context != null) {
 //           val resources = context.resources
 //       }

//    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_user, container, false)
        root.recyclerViewU.setLayoutManager(LinearLayoutManager(activity))

        selectedEvent = arguments!!.getString("selectedEvent").toString()
       // info("is there a selected User ${userSelected}")
       setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = root.recyclerViewU.adapter as UserAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                addUserToEvent(selectedEvent,
                    (viewHolder.itemView.tag as User).uUid)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(root.recyclerViewF)

        val swipeEditHandler = object : SwipeToEditCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onUserClick(viewHolder.itemView.tag as User)
            }
        }

        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(root.recyclerViewU)

        return root
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AdminSelectUserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AdminSelectUserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onUserClick(user: User) {

        val args = Bundle()
        //   args.putParcelable("selectedUser",user )
        args.putString("selectedUser",user.uUid )
        var frag = AboutUsFragment.newInstance()
        frag.arguments=args
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame,frag )
            .addToBackStack(null)
            .commit()
    }



    fun getAllUsers() {
  //      Toast.makeText(activity, "Get All Users", Toast.LENGTH_LONG).show()
        //    loader = createLoader(activity!!)
        //    showLoader(loader, "Downloading Donations from Firebase")
  //      val userList = ArrayList<User>()
  //      context?.resources.refHF.addValueEventListener(object : ValueEventListener {
    //            override fun onCancelled(error: DatabaseError) {
     //              // info("Firebase Donation error : ${error.message}")
     //           }
     //           override fun onDataChange(snapshot: DataSnapshot) {
     //               //      hideLoader(loader)
     //               val children = snapshot!!.children
     //               children.forEach {
     //                   val user = it.
      //                      getValue<User>(User::class.java)
//
  ///                      userList.add(user!!)
   //                     root.recyclerViewU.adapter =
    //                        UserAdapter(userList, this@AdminSelectUserFragment)
      //                  root.recyclerViewU.adapter?.notifyDataSetChanged()
       //                 checkSwipeRefresh()
        //                //    app.database.child("users").child(userId).child("placemarks")
         //               db.child("users")
           //                 .removeEventListener(this)
             //       }
               // }
         //   })
    }



    fun addUserToEvent(userId: String, uid: String?) {
        //  db.child("user").child(userSelected).child(uid!!)
    //    db.child("user")
      //      .addListenerForSingleValueEvent(
        ///        object : ValueEventListener {
           //         override fun onDataChange(snapshot: DataSnapshot) {
             //           db.child("users").setValue(userSelected)
               //         db.child("users").child(userSelected).setValue(uid)
                 //       //  snapshot.ref.removeValue()
                   // }
                  //  override fun onCancelled(error: DatabaseError) {
                  //      info("Firebase Placemarks error : ${error.message}")
                  //  }
  //              })
    }



    fun setSwipeRefresh() {
     //   info("In set swipeRefresh")
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                //  placemarksListFrag = app.pObj.findAll2()
                getAllUsers()
            }
        })
    }


    fun checkSwipeRefresh() {
        if (root.swiperefresh.isRefreshing) root.swiperefresh.isRefreshing = false
    }


}
