package com.clmf.player.utils

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMapper {

    fun map(throwable: Throwable): AppError {
        AppLogger.error("Mapping error: ${throwable.javaClass.simpleName}", throwable)
        return when (throwable) {
            is SocketTimeoutException -> AppError.Timeout
            is UnknownHostException -> AppError.ServerUnavailable
            is HttpException -> when (throwable.code()) {
                401, 403 -> AppError.InvalidCredentials
                in 500..599 -> AppError.ServerUnavailable
                else -> AppError.Http(throwable.code())
            }
            is IOException -> AppError.NoInternet
            else -> AppError.Unknown(throwable.message)
        }
    }
}
