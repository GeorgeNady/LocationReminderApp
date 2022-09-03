package com.udacity.project4.db.dto


/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and statusCode
 */
sealed class Resources<out T : Any> {
    data class Success<out T : Any>(val data: T) : Resources<T>()
    data class Error(val message: String?, val statusCode: Int? = null) : Resources<Nothing>()
}