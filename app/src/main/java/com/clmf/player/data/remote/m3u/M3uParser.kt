package com.clmf.player.data.remote.m3u

/**
 * Parses M3U/M3U8 playlists that follow the common `#EXTINF` convention used
 * by most IPTV panels (tvg-id, tvg-name, tvg-logo, group-title). This exists
 * so a future M3uProvider can implement [com.clmf.player.domain.repository.IPTVProvider]
 * alongside the Xtream Codes provider without touching the rest of the app.
 */
object M3uParser {

    private val attributeRegex = Regex("(\\S+)=\"([^\"]*)\"")

    /**
     * Parses from an in-memory string. Convenient for tests and small
     * playlists, but real IPTV panels can serve playlists tens of megabytes
     * long — prefer [parse] with a [Sequence] (e.g. `BufferedReader.lineSequence()`)
     * for those so the whole file is never held in memory as one String.
     */
    fun parse(playlist: String): List<M3uEntry> = parse(playlist.lineSequence())

    fun parse(lines: Sequence<String>): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        var pendingInfo: M3uEntry? = null

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            when {
                line.startsWith("#EXTINF:") -> pendingInfo = parseExtInf(line)
                line.startsWith("#") -> Unit // ignore other directives (#EXTM3U, #EXTGRP, etc.)
                else -> {
                    val info = pendingInfo
                    if (info != null) {
                        entries += info.copy(streamUrl = line)
                        pendingInfo = null
                    }
                }
            }
        }
        return entries
    }

    private fun parseExtInf(line: String): M3uEntry {
        val attributes = attributeRegex.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
        val name = line.substringAfterLast(",").trim()
        return M3uEntry(
            name = name.ifBlank { "Sem nome" },
            tvgId = attributes["tvg-id"],
            tvgName = attributes["tvg-name"],
            logoUrl = attributes["tvg-logo"],
            groupTitle = attributes["group-title"],
            streamUrl = ""
        )
    }
}

data class M3uEntry(
    val name: String,
    val tvgId: String?,
    val tvgName: String?,
    val logoUrl: String?,
    val groupTitle: String?,
    val streamUrl: String
)
