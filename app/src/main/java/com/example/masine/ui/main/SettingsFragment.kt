package com.example.masine.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.masine.BuildConfig
import com.example.masine.MainActivity
import com.example.masine.databinding.FragmentSettingsBinding
import com.example.masine.scripts.Application
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.util.concurrent.Executors


class SettingsFragment : Fragment() {
    private lateinit var app: Application
    private lateinit var binding: FragmentSettingsBinding

    private lateinit var imageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = requireActivity().application as Application;
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = app.getStorage("vehicleName")
        binding.vehicleNameSettingsInput.setText(name)

        val address = app.getStorage("address")
        binding.mqttAddressInput.setText(address)

        binding.saveButton.setOnClickListener {
            if((binding.vehicleNameSettingsInput.text.toString() == "" || binding.vehicleNameSettingsInput.text.toString() == name) && (binding.mqttAddressInput.text.toString() == "") || binding.mqttAddressInput.text.toString() == address) {
                onError("Invalid or same")
                return@setOnClickListener
            }

            app.setStorage("address", binding.mqttAddressInput.text.toString())
            app.setStorage("vehicleName", binding.vehicleNameSettingsInput.text.toString())

            (activity as MainActivity?)!!.replaceFragment(SettingsFragment())
        }

        binding.getButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            /*val photo: File
            try {
                val outFile = requireContext().cacheDir;
                photo = File.createTempFile("picture", ".jpg", outFile)

                //photo = this.createTemporaryFile("picture", ".jpg")!!
                //photo.delete()
            } catch (e: Exception) {
                throw e
                onError("Cannot create tmp file!")
                return@setOnClickListener
            }*/
            val photo = createEmptyImageTempFile(requireContext())
            imageUri = FileProvider.getUriForFile(requireActivity(), "com.example.masine.provider", photo);
            //imageUri = Uri.fromFile(photo)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

            resultLauncher.launch(cameraIntent)
        }
    }

    private fun onError(message: String){
        val error = Snackbar.make(binding.root, message, 1500)
        error.view.setBackgroundColor(Color.CYAN)
        error.show()
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            var image : Bitmap? = null
            try {
                imageUri.let {
                    val source = ImageDecoder.createSource(requireActivity().contentResolver, imageUri)
                    image = ImageDecoder.decodeBitmap(source)
                }

            }
            catch (e: java.lang.Exception) {
                onError("Cannot create get image!")
                return@registerForActivityResult
            }

            //val data = result.data
            //val image : Bitmap = data?.extras?.get("data") as Bitmap

            Log.d("test", image!!.height.toString())
            val baos = ByteArrayOutputStream()
            image!!.compress(Bitmap.CompressFormat.PNG, 100, baos);
            val b = baos.toByteArray();

            val executor = Executors.newSingleThreadExecutor();
            val handler = Handler(Looper.getMainLooper());
            executor.execute{
                val client = OkHttpClient()

                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("image", "filename.jpg",
                    b.toRequestBody("image/*jpg".toMediaTypeOrNull(), 0, b.size)
                ).build()

                val request = Request.Builder().url("http://${app.getStorage("address").split("//")[1].split(':')[0]}:6000/licence_plate").post(requestBody).build()

                try {
                    val res = client.newCall(request).execute().body!!.string();

                    val text = JSONObject(res).getString("text")
                    if(text != "" || text.length < 4) {
                        handler.post {
                            onError("Invalid response!")
                        }
                    }
                    else{
                        app.setStorage("vehicleName", text)
                        handler.post {
                            binding.vehicleNameSettingsInput.setText(text)
                        }
                    }
                }
                catch (_ : Exception){
                    handler.post {
                        onError("Invalid image!")
                    }
                }
            };

        }
    }

    private val CAMERA_IMAGE_FILE_NAME = "image.tmp"

    private fun createEmptyImageTempFile(context : Context) : File {
        val f = File(context.filesDir, CAMERA_IMAGE_FILE_NAME);
        f.delete();
        var fos : FileOutputStream? = null;
        try {
            fos = context.openFileOutput(CAMERA_IMAGE_FILE_NAME, Context.MODE_PRIVATE);
        } catch (e : FileNotFoundException) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (e : IOException) {
                    e.printStackTrace();
                }
            }
        }

        return getImageTempFile(context);
    }

    private fun getImageTempFile(context : Context) : File {
        return File(context.filesDir, "image.tmp");
    }
}