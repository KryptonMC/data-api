package org.kryptonmc.data.util

import retrofit2.Call

fun <T> Call<T>.executeSuccess(): T {
    val response = execute()
    require(response.isSuccessful) { "Request unsuccessful! Request: ${request()}, Code: ${response.code()}, Error body: ${response.errorBody()?.string()}" }
    return response.body()!!
}
