package com.clmf.player.data.remote.api

import kotlinx.serialization.json.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Xtream Codes exposes a single `player_api.php` endpoint whose response
 * shape depends entirely on the `action` query parameter, so responses are
 * decoded as raw [JsonElement] and mapped explicitly in the provider.
 */
interface XtreamApi {

    @GET
    suspend fun call(
        @Url fullUrl: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String? = null,
        @Query("category_id") categoryId: String? = null,
        @Query("vod_id") vodId: String? = null,
        @Query("series_id") seriesId: String? = null,
        @Query("stream_id") streamId: String? = null
    ): JsonElement
}
