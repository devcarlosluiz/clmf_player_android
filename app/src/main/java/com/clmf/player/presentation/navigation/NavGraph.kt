package com.clmf.player.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.clmf.player.presentation.favorites.FavoritesScreen
import com.clmf.player.presentation.home.HomeScreen
import com.clmf.player.presentation.license.LicenseScreen
import com.clmf.player.presentation.live.LiveTvScreen
import com.clmf.player.presentation.login.LoginScreen
import com.clmf.player.presentation.movies.MovieDetailScreen
import com.clmf.player.presentation.movies.MoviesScreen
import com.clmf.player.presentation.player.PlayerScreen
import com.clmf.player.presentation.playlists.AddPlaylistScreen
import com.clmf.player.presentation.playlists.PlaylistsScreen
import com.clmf.player.presentation.search.SearchScreen
import com.clmf.player.presentation.series.EpisodesScreen
import com.clmf.player.presentation.series.SeriesDetailScreen
import com.clmf.player.presentation.series.SeriesScreen
import com.clmf.player.presentation.settings.SettingsScreen
import com.clmf.player.presentation.splash.SplashScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val SPLASH = "splash"
    const val LICENSE = "license"
    const val LOGIN = "login"
    const val HOME = "home"
    const val LIVE_TV = "live_tv"
    const val MOVIES = "movies"
    const val MOVIE_DETAIL = "movie_detail/{movieId}"
    const val SERIES = "series"
    const val SERIES_DETAIL = "series_detail/{seriesId}"
    const val EPISODES = "episodes/{seriesId}/{seasonNumber}"
    const val FAVORITES = "favorites"
    const val PLAYLISTS = "playlists"
    const val ADD_PLAYLIST = "add_playlist"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val PLAYER = "player/{contentType}/{contentId}/{streamUrl}/{title}"

    fun movieDetail(movieId: String) = "movie_detail/$movieId"
    fun seriesDetail(seriesId: String) = "series_detail/$seriesId"
    fun episodes(seriesId: String, seasonNumber: Int) = "episodes/$seriesId/$seasonNumber"
    fun player(contentType: String, contentId: String, streamUrl: String, title: String): String {
        val encodedUrl = URLEncoder.encode(streamUrl, "UTF-8")
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        return "player/$contentType/$contentId/$encodedUrl/$encodedTitle"
    }
}

@androidx.compose.runtime.Composable
fun CLMFNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable(Routes.SPLASH) { SplashScreen(navController) }
        composable(Routes.LICENSE) { LicenseScreen(navController) }
        composable(Routes.LOGIN) { LoginScreen(navController) }
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.LIVE_TV) { LiveTvScreen(navController) }
        composable(Routes.MOVIES) { MoviesScreen(navController) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController) }
        composable(Routes.PLAYLISTS) { PlaylistsScreen(navController) }
        composable(Routes.ADD_PLAYLIST) { AddPlaylistScreen(navController) }
        composable(Routes.SEARCH) { SearchScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
        composable(Routes.SERIES) { SeriesScreen(navController) }

        composable(
            Routes.MOVIE_DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId").orEmpty()
            MovieDetailScreen(navController, movieId)
        }

        composable(
            Routes.SERIES_DETAIL,
            arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId").orEmpty()
            SeriesDetailScreen(navController, seriesId)
        }

        composable(
            Routes.EPISODES,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("seasonNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId").orEmpty()
            val seasonNumber = backStackEntry.arguments?.getInt("seasonNumber") ?: 1
            EpisodesScreen(navController, seriesId, seasonNumber)
        }

        composable(
            Routes.PLAYER,
            arguments = listOf(
                navArgument("contentType") { type = NavType.StringType },
                navArgument("contentId") { type = NavType.StringType },
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments
            val contentType = args?.getString("contentType").orEmpty()
            val contentId = args?.getString("contentId").orEmpty()
            val streamUrl = URLDecoder.decode(args?.getString("streamUrl").orEmpty(), "UTF-8")
            val title = URLDecoder.decode(args?.getString("title").orEmpty(), "UTF-8")
            PlayerScreen(navController, contentType, contentId, streamUrl, title)
        }
    }
}
