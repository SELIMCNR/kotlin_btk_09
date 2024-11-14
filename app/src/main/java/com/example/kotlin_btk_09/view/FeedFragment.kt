package com.example.kotlin_btk_09.view

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlin_btk_09.model.Post
import com.example.kotlin_btk_09.R
import com.example.kotlin_btk_09.adapter.PostAdapter
import com.example.kotlin_btk_09.databinding.FragmentFeedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore

class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var popup: PopupMenu
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    val postList : ArrayList<Post> = arrayListOf()
    private var adapter : PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize popup menu only once
        popup = PopupMenu(requireContext(), binding.floatingActionButton)
        popup.menuInflater.inflate(R.menu.my_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)

        binding.floatingActionButton.setOnClickListener {
            popup.show()
        }

        fireStoreVerileriniAl()

        adapter = PostAdapter(postList)
       binding.feedrecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedrecyclerView.adapter = adapter

    }

    private  fun fireStoreVerileriniAl(){
        db.collection("Posts").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()

            }
            else {
                if (value !=null){
                    if (!value.isEmpty){
                        //boş değilse
                        postList.clear()
                        val documents = value.documents
                        for (document in documents){
                            val comment = document.get("comment") as String
                            val email = document.get("email") as String
                            val downloadUrl = document.get("downloadUrl") as String
                            println(comment)
                            Log.d("FeedFragment", "Download URL: $downloadUrl")
                            val post = Post(email,comment,downloadUrl)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()

                    }
                }
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.yuklemeItem -> {
                val action = FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
                Navigation.findNavController(requireView()).navigate(action)
            }
            R.id.cikisItem -> {
                auth.signOut()
                val action = FeedFragmentDirections.actionFeedFragmentToKullaniciFragment()
                Navigation.findNavController(requireView()).navigate(action)
            }
        }
        return true
    }
}
