package com.example.kotlin_btk_09.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.kotlin_btk_09.databinding.FragmentYuklemeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.UUID


class YuklemeFragment : Fragment() {

    private  var _binding : FragmentYuklemeBinding? = null

    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    private lateinit var auth : FirebaseAuth
    private  lateinit var storage : FirebaseStorage
    private  lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonYukle.setOnClickListener {
            yukleTiklandi(it)
        }

        binding.imageView.setOnClickListener {
            gorselSec(it)
        }

    }
    fun yukleTiklandi(view : View){

        val uuid = UUID.randomUUID()
        val gorselAdi = "${uuid}.jpg"

        val reference = storage.reference
        val gorselReference = reference.child(gorselAdi)

        secilenGorsel?.let {
            gorselReference.putFile(it).addOnSuccessListener {
                gorselReference.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()

                    // Veri tabanına kayıt işlemi
                    val postMap = hashMapOf<String, Any>(
                        "downloadUrl" to downloadUrl,
                        "email" to auth.currentUser?.email.toString(),
                        "comment" to binding.editCommentText.text.toString(),
                        "date" to Timestamp.now()
                    )

                    db.collection("Posts").add(postMap)
                        .addOnSuccessListener {
                            val action = YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Download URL alınamadı: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Görsel yükleme başarısız: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        } ?: Toast.makeText(requireContext(), "Görsel seçilmedi", Toast.LENGTH_LONG).show()


    }
    fun gorselSec(view: View){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // read media images

            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED){
                //izin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    //izni almayı göster
                    Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                    ,View.OnClickListener {
                        // izin iste
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                        }
                    ).show()
                }
                else {
                    //izin iste
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

             }
            }
            else {
                //izin var
                //galeriye gitme kodu
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)


            }

        }
        else{
            //read external storage
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                //izin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //izni almayı göster
                    Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                        ,View.OnClickListener {
                            // izin iste
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                        }
                    ).show()
                }
                else {
                    //izin iste
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }
            }
            else {
                //izin var
                //galeriye gitme kodu
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)


            }
        }

    }

    private  fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->

            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    secilenGorsel = it

                    try {

                        if (Build.VERSION.SDK_INT>=28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver ,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                        else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }


                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }

                }


            }


        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        {
            result ->
            if (result){
                // izin verildi
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else {
                // izin verilmedi
                Toast.makeText(requireContext(),"İzin Gerekli",Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}