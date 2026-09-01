package com.clmf.player

import com.clmf.player.data.remote.m3u.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Test

class M3uParserTest {

    @Test
    fun `parses standard EXTINF entries with attributes`() {
        val playlist = """
            #EXTM3U
            #EXTINF:-1 tvg-id="globo.br" tvg-name="Globo" tvg-logo="http://logo/globo.png" group-title="Abertos",Globo HD
            http://server.com/live/user/pass/1.m3u8
            #EXTINF:-1 tvg-id="sportv.br" group-title="Esportes",SporTV
            http://server.com/live/user/pass/2.m3u8
        """.trimIndent()

        val entries = M3uParser.parse(playlist)

        assertEquals(2, entries.size)
        assertEquals("Globo HD", entries[0].name)
        assertEquals("Abertos", entries[0].groupTitle)
        assertEquals("http://server.com/live/user/pass/1.m3u8", entries[0].streamUrl)
        assertEquals("SporTV", entries[1].name)
        assertEquals("Esportes", entries[1].groupTitle)
    }

    @Test
    fun `ignores blank lines and unknown directives`() {
        val playlist = """
            #EXTM3U

            #EXTGRP:Custom

            #EXTINF:-1,Canal Simples
            http://server.com/live/user/pass/3.m3u8
        """.trimIndent()

        val entries = M3uParser.parse(playlist)

        assertEquals(1, entries.size)
        assertEquals("Canal Simples", entries[0].name)
    }

    @Test
    fun `returns empty list for playlist without stream urls`() {
        val playlist = "#EXTM3U\n#EXTINF:-1,Canal sem stream"
        assertEquals(0, M3uParser.parse(playlist).size)
    }
}
