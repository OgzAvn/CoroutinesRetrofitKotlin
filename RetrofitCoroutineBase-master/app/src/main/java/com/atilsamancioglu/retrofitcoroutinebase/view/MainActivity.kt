package com.atilsamancioglu.retrofitcoroutinebase.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atilsamancioglu.retrofitcoroutinebase.R
import com.atilsamancioglu.retrofitcoroutinebase.adapter.RecyclerViewAdapter
import com.atilsamancioglu.retrofitcoroutinebase.databinding.ActivityMainBinding
import com.atilsamancioglu.retrofitcoroutinebase.model.CryptoModel
import com.atilsamancioglu.retrofitcoroutinebase.service.CryptoAPI
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(),RecyclerViewAdapter.Listener {


    private lateinit var binding: ActivityMainBinding
    private val BASE_URL = "https://raw.githubusercontent.com/"
    private var cryptoModels: ArrayList<CryptoModel>? = null
    private var recyclerViewAdapter : RecyclerViewAdapter? = null
    private var job : Job? = null

    val exceptionHandler = CoroutineExceptionHandler{ coroutineContext, throwable ->

        println("Error : ${throwable.localizedMessage}")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //RecyclerView

        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager

        loadData()


    }

    private fun loadData() {

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CryptoAPI::class.java)


        //Internet üzerinden veri çekeceğimiz için Dispatchers.IO kullandık
        job = CoroutineScope(Dispatchers.IO).launch {

            val response = retrofit.getData()

            withContext(Dispatchers.Main + exceptionHandler){

                if (response.isSuccessful){

                    response.body()?.let {

                        cryptoModels = ArrayList(it)
                        cryptoModels?.let {

                            recyclerViewAdapter = RecyclerViewAdapter(it,this@MainActivity)
                            binding.recyclerView.adapter = recyclerViewAdapter
                        }
                    }
                }
            }
        }


        /*
        val call = retrofit.getData()

         call.enqueue(object: Callback<List<CryptoModel>> {
            override fun onFailure(call: Call<List<CryptoModel>>, t: Throwable) {
                t.printStackTrace()
            }
            override fun onResponse(
                call: Call<List<CryptoModel>>,
                response: Response<List<CryptoModel>>
            ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            cryptoModels = ArrayList(it)
                            cryptoModels?.let {
                                recyclerViewAdapter = RecyclerViewAdapter(it,this@MainActivity)
                                binding.recyclerView.adapter = recyclerViewAdapter
                            }
                        }
                    }
            }
        })

         */



    }

    override fun onItemClick(cryptoModel: CryptoModel) {
        Toast.makeText(applicationContext,"Clicked on: ${cryptoModel.currency}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

}
