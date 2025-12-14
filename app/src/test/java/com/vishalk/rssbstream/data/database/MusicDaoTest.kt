package com.vishalk.rssbstream.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MusicDaoTest {

    private lateinit var musicDao: MusicDao
    private lateinit var db: RssbStreamDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RssbStreamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        musicDao = db.musicDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetSongs() = runTest {
        val songList = listOf(
            SongEntity(1L, "Song A", "Artist 1", 101L, "Album X", 201L, "uri_a", "art_uri_a", 180000, "Pop", "/path/a", "/path"),
            SongEntity(2L, "Song B", "Artist 2", 102L, "Album Y", 202L, "uri_b", "art_uri_b", 240000, "Rock", "/path/b", "/path")
        )
        musicDao.insertSongs(songList)

        // getSongs has signature: getSongs(allowedParentDirs: List<String>, applyDirectoryFilter: Boolean)
        val retrievedSongs = musicDao.getSongs(emptyList(), false).first()
        assertThat(retrievedSongs).hasSize(2)
        assertThat(retrievedSongs).containsExactlyElementsIn(songList.sortedBy { it.title })
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetAlbums() = runTest {
        val albumList = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 5, 2020),
            AlbumEntity(202L, "Album Y", "Artist 2", 102L, "art_uri_y", 8, 2021)
        )
        musicDao.insertAlbums(albumList)

        // getAlbums uses INNER JOIN songs, so we must insert songs linked to these albums
        val songs = listOf(
            SongEntity(1L, "S1", "Artist 1", 101L, "Album X", 201L, "u", null, 0, "G", "p", "pd"),
            SongEntity(2L, "S2", "Artist 2", 102L, "Album Y", 202L, "u", null, 0, "G", "p", "pd")
        )
        musicDao.insertSongs(songs)

        val retrievedAlbums = musicDao.getAlbums(emptyList(), false).first()
        assertThat(retrievedAlbums).hasSize(2)
        assertThat(retrievedAlbums).containsExactlyElementsIn(albumList.sortedBy { it.title })
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetArtists() = runTest {
        val artistList = listOf(
            ArtistEntity(101L, "Artist 1", 10),
            ArtistEntity(102L, "Artist 2", 15)
        )
        musicDao.insertArtists(artistList)

        // getArtists uses INNER JOIN songs, so we must insert songs linked to these artists
        val songs = listOf(
            SongEntity(1L, "S1", "Artist 1", 101L, "Album X", 201L, "u", null, 0, "G", "p", "pd"),
            SongEntity(2L, "S2", "Artist 2", 102L, "Album Y", 202L, "u", null, 0, "G", "p", "pd")
        )
        musicDao.insertSongs(songs)

        val retrievedArtists = musicDao.getArtists(emptyList(), false).first()
        assertThat(retrievedArtists).hasSize(2)
        assertThat(retrievedArtists).containsExactlyElementsIn(artistList.sortedBy { it.name })
    }

    @Test
    @Throws(Exception::class)
    fun insertMusicData_clearsOldAndInsertsNew() = runTest {
        val oldSong = SongEntity(1L, "Old Song", "Old Artist", 1L, "Old Album", 1L, "old_uri", null, 100, "Genre", "/old/path", "/old")
        musicDao.insertSongs(listOf(oldSong))

        val songs = listOf(
            SongEntity(10L, "Song A", "Artist 1", 101L, "Album X", 201L, "uri_a", "art_uri_a", 180000, "Pop", "/path/a", "/path")
        )
        val albums = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 1, 2022)
        )
        val artists = listOf(
            ArtistEntity(101L, "Artist 1", 1)
        )

        musicDao.insertMusicData(songs, albums, artists)

        assertThat(musicDao.getSongById(1L).first()).isNull() // Old song should be gone
        assertThat(musicDao.getSongById(10L).first()).isNotNull()
        assertThat(musicDao.getAlbumById(201L).first()).isNotNull()
        assertThat(musicDao.getArtistById(101L).first()).isNotNull()
    }

    @Test
    @Throws(Exception::class)
    fun searchSongs_returnsMatchingSongs() = runTest {
        val songs = listOf(
            SongEntity(1L, "Cool Song", "Artist A", 101L, "Album X", 201L, "uri1", null, 180, "Pop", "/p1", "/p"),
            SongEntity(2L, "Another Song", "Artist B", 102L, "Album Y", 202L, "uri2", null, 200, "Rock", "/p2", "/p"),
            SongEntity(3L, "Coolest Song Ever", "Artist C", 103L, "Album Z", 203L, "uri3", null, 220, "Pop", "/p3", "/p")
        )
        musicDao.insertSongs(songs)

        val results = musicDao.searchSongs("Cool", emptyList(), false).first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Cool Song", "Coolest Song Ever")
    }

    // --- New Tests ---

    @Test
    @Throws(Exception::class)
    fun getSongsByIds_returnsRequestedSongs() = runTest {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist A", 101L, "Album X", 201L, "uri1", null, 180, "Pop", "/p1", "/p"),
            SongEntity(2L, "Song 2", "Artist B", 102L, "Album Y", 202L, "uri2", null, 200, "Rock", "/p2", "/p"),
            SongEntity(3L, "Song 3", "Artist C", 103L, "Album Z", 203L, "uri3", null, 220, "Pop", "/p3", "/p")
        )
        musicDao.insertSongs(songs)

        val results = musicDao.getSongsByIds(listOf(1L, 3L), emptyList(), false).first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Song 1", "Song 3")
    }

    @Test
    @Throws(Exception::class)
    fun getSongsByAlbumId_returnsSongsForAlbum() = runTest {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist A", 101L, "Album X", 201L, "uri1", null, 180, "Pop", "/p1", "/p"),
            SongEntity(2L, "Song 2", "Artist A", 101L, "Album X", 201L, "uri2", null, 200, "Pop", "/p2", "/p"),
            SongEntity(3L, "Song 3", "Artist B", 102L, "Album Y", 202L, "uri3", null, 220, "Rock", "/p3", "/p")
        )
        musicDao.insertSongs(songs)

        val results = musicDao.getSongsByAlbumId(201L).first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Song 1", "Song 2")
    }

    @Test
    @Throws(Exception::class)
    fun getSongsByArtistId_returnsSongsForArtist() = runTest {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist A", 101L, "Album X", 201L, "uri1", null, 180, "Pop", "/p1", "/p"),
            SongEntity(2L, "Song 2", "Artist A", 101L, "Album X", 201L, "uri2", null, 200, "Pop", "/p2", "/p"),
            SongEntity(3L, "Song 3", "Artist B", 102L, "Album Y", 202L, "uri3", null, 220, "Rock", "/p3", "/p")
        )
        musicDao.insertSongs(songs)

        val results = musicDao.getSongsByArtistId(101L).first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Song 1", "Song 2")
    }

    @Test
    @Throws(Exception::class)
    fun getSongCount_returnsTotalCount() = runTest {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist A", 101L, "Album X", 201L, "uri1", null, 180, "Pop", "/p1", "/p"),
            SongEntity(2L, "Song 2", "Artist B", 102L, "Album Y", 202L, "uri2", null, 200, "Rock", "/p2", "/p")
        )
        musicDao.insertSongs(songs)

        val count = musicDao.getSongCount().first()
        assertThat(count).isEqualTo(2)
    }

    @Test
    @Throws(Exception::class)
    fun getAlbumById_returnsCorrectAlbum() = runTest {
        val albums = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 5, 2020),
            AlbumEntity(202L, "Album Y", "Artist 2", 102L, "art_uri_y", 8, 2021)
        )
        musicDao.insertAlbums(albums)

        val result = musicDao.getAlbumById(201L).first()
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Album X")

        val resultNull = musicDao.getAlbumById(999L).first()
        assertThat(resultNull).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun searchAlbums_returnsMatchingAlbums() = runTest {
        val albums = listOf(
            AlbumEntity(201L, "Rock Classics", "Artist 1", 101L, "art_uri_x", 5, 2020),
            AlbumEntity(202L, "Pop Hits", "Artist 2", 102L, "art_uri_y", 8, 2021),
            AlbumEntity(203L, "Rock Legends", "Artist 3", 103L, "art_uri_z", 10, 2019)
        )
        musicDao.insertAlbums(albums)

        // Using simple searchAlbums which searches by title without checking song existence
        val results = musicDao.searchAlbums("Rock").first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Rock Classics", "Rock Legends")
    }

    @Test
    @Throws(Exception::class)
    fun getAlbumCount_returnsTotalCount() = runTest {
        val albums = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 5, 2020),
            AlbumEntity(202L, "Album Y", "Artist 2", 102L, "art_uri_y", 8, 2021)
        )
        musicDao.insertAlbums(albums)

        val count = musicDao.getAlbumCount().first()
        assertThat(count).isEqualTo(2)
    }

    @Test
    @Throws(Exception::class)
    fun getAlbumsByArtistId_returnsAlbumsForArtist() = runTest {
        val albums = listOf(
            AlbumEntity(201L, "Album X", "Artist 1", 101L, "art_uri_x", 5, 2020),
            AlbumEntity(202L, "Album Y", "Artist 1", 101L, "art_uri_y", 8, 2021),
            AlbumEntity(203L, "Album Z", "Artist 2", 102L, "art_uri_z", 10, 2019)
        )
        musicDao.insertAlbums(albums)

        val results = musicDao.getAlbumsByArtistId(101L).first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.title }).containsExactly("Album X", "Album Y")
    }

    @Test
    @Throws(Exception::class)
    fun getArtistById_returnsCorrectArtist() = runTest {
        val artists = listOf(
            ArtistEntity(101L, "Artist 1", 10),
            ArtistEntity(102L, "Artist 2", 15)
        )
        musicDao.insertArtists(artists)

        val result = musicDao.getArtistById(101L).first()
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("Artist 1")

        val resultNull = musicDao.getArtistById(999L).first()
        assertThat(resultNull).isNull()
    }

    @Test
    @Throws(Exception::class)
    fun searchArtists_returnsMatchingArtists() = runTest {
        val artists = listOf(
            ArtistEntity(101L, "John Doe", 10),
            ArtistEntity(102L, "Jane Doe", 15),
            ArtistEntity(103L, "Bob Smith", 5)
        )
        musicDao.insertArtists(artists)

        // Using simple searchArtists which searches by name
        val results = musicDao.searchArtists("Doe").first()
        assertThat(results).hasSize(2)
        assertThat(results.map { it.name }).containsExactly("Jane Doe", "John Doe")
    }

    @Test
    @Throws(Exception::class)
    fun getArtistCount_returnsTotalCount() = runTest {
        val artists = listOf(
            ArtistEntity(101L, "Artist 1", 10),
            ArtistEntity(102L, "Artist 2", 15)
        )
        musicDao.insertArtists(artists)

        val count = musicDao.getArtistCount().first()
        assertThat(count).isEqualTo(2)
    }

    // TODO: Add more tests for other DAO methods:
    // - getSongsByGenre, getUniqueGenres
    // - getAllUniqueAlbumArtUrisFromSongs
    // - Test pagination (offset, pageSize) thoroughly
    // - Test onConflictStrategy (REPLACE) for inserts
}
