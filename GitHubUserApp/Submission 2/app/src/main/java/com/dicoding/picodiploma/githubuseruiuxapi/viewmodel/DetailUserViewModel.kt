package com.dicoding.picodiploma.githubuseruiuxapi.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.githubuseruiuxapi.BuildConfig
import com.dicoding.picodiploma.githubuseruiuxapi.model.UserDetail
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class DetailUserViewModel : ViewModel() {

    companion object {
        private val TAG = DetailUserViewModel::class.java.simpleName
        private const val GITHUB_API = BuildConfig.GithubAPI
    }
    private val detailUser = MutableLiveData<UserDetail>()
    private lateinit var errorMsg: String

    fun setDetailUser(username: String, context: Context) {
        val userUrl = "https://api.github.com/users/$username"
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "token $GITHUB_API")
        client.addHeader("User-Agent", "request")
        client.get(userUrl, object : AsyncHttpResponseHandler(){
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                try {
                    val result = responseBody?.let { String(it) }
                    Log.d(TAG, result!!)
                    val responseObject = JSONObject(result)
                    val dataName = responseObject.getString("name")
                    val dataCompany = responseObject.getString("company")
                    val dataLocation = responseObject.getString("location")
                    val dataRepositories = responseObject.getInt("public_repos")
                    val dataFollowers = responseObject.getInt("followers")
                    val dataFollowing = responseObject.getInt("following")
                    val userDetail = UserDetail(
                            dataName,
                            dataCompany,
                            dataLocation,
                            dataRepositories,
                            dataFollowers,
                            dataFollowing
                    )
                    detailUser.postValue(userDetail)
                } catch (e: Exception) {
                    Log.d(TAG, e.message.toString())
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                errorMsg = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error?.message}"
                }
                Log.d(TAG, errorMsg)
                GlobalScope.launch(Dispatchers.Main) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    fun getDetailUser(): LiveData<UserDetail> = detailUser
}