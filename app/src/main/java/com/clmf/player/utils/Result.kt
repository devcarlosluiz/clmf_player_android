package com.clmf.player.utils

/** Lightweight result wrapper used across repositories and use cases. */
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
}

/** User-facing error taxonomy. Never expose stack traces to the UI layer. */
sealed class AppError(val friendlyMessage: String) {
    data object NoInternet : AppError("Sem conexão com a internet. Verifique sua rede e tente novamente.")
    data object ServerUnavailable : AppError("Não foi possível conectar ao servidor. Verifique o endereço do servidor.")
    data object InvalidCredentials : AppError("Usuário ou senha inválidos.")
    data object Timeout : AppError("O servidor demorou muito para responder. Tente novamente.")
    data object EmptyPlaylist : AppError("Nenhum conteúdo encontrado nesta lista.")
    data object StreamUnavailable : AppError("Não foi possível reproduzir este conteúdo no momento.")
    data object EpgUnavailable : AppError("Guia de programação indisponível para este canal.")
    data class Http(val code: Int) : AppError("Erro do servidor (código $code). Tente novamente mais tarde.")
    data class Unknown(val debugMessage: String? = null) : AppError("Ocorreu um erro inesperado. Tente novamente.")
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <T> AppResult<T>.onError(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Error) action(error)
    return this
}
