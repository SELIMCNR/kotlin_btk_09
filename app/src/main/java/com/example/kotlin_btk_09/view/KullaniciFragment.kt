package com.example.kotlin_btk_09.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.kotlin_btk_09.KullaniciFragmentDirections.Companion.actionKullaniciFragmentToFeedFragment
import com.example.kotlin_btk_09.databinding.FragmentKullaniciBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class KullaniciFragment : Fragment() {

    private  var _binding : FragmentKullaniciBinding? = null


    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKullaniciBinding.inflate(inflater,container,false)
        val view = binding.root
        return  view
   }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonKayTol.setOnClickListener {
            kayitOl(it)
        }
        binding.buttonGirisYap.setOnClickListener {
            girisYap(it)
        }

        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null){
            val action = actionKullaniciFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun kayitOl(view : View){
        val email = binding.textEmail.text.toString()
        val password = binding.textPassword.text.toString()
    if (email.isNotEmpty() && password.isNotEmpty())
    {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                    task ->  //complete:tamamlanırsa

                if (task.isSuccessful){

                    val action = actionKullaniciFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)

                }

            }.addOnFailureListener {
                exception ->  //failure:hata oluşursa
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()

                println(exception.localizedMessage)
            }

    }


   }

    fun girisYap(view: View){
        val email = binding.textEmail.text.toString()
        val password = binding.textPassword.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty())
        {
            auth.signInWithEmailAndPassword (email,password)
                .addOnSuccessListener { //SUCCES : BAŞARILI
                        task ->

                        val action = actionKullaniciFragmentToFeedFragment()
                        Navigation.findNavController(view).navigate(action)



                }.addOnFailureListener {
                        exception ->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()

                    println(exception.localizedMessage)
                }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}