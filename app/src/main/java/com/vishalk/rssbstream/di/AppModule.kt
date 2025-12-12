package com.vishalk.rssbstream.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.room.Room
import coil.ImageLoader
import com.vishalk.rssbstream.RssbStreamApplication
import com.vishalk.rssbstream.data.database.AlbumArtThemeDao
import com.vishalk.rssbstream.data.database.MusicDao
import com.vishalk.rssbstream.data.database.RssbStreamDatabase
import com.vishalk.rssbstream.data.database.RssbContentDao
import com.vishalk.rssbstream.data.database.SearchHistoryDao
import com.vishalk.rssbstream.data.database.TransitionDao
import com.vishalk.rssbstream.data.network.ContentCatalogApi
import com.vishalk.rssbstream.data.network.R2Config
import com.vishalk.rssbstream.data.preferences.PreferencesManager
import com.vishalk.rssbstream.data.preferences.UserPreferencesRepository
import com.vishalk.rssbstream.data.preferences.dataStore
import com.vishalk.rssbstream.data.media.SongMetadataEditor
import com.vishalk.rssbstream.data.network.lyrics.LrcLibApiService
import com.vishalk.rssbstream.data.repository.LyricsRepository
import com.vishalk.rssbstream.data.repository.LyricsRepositoryImpl
import com.vishalk.rssbstream.data.repository.MusicRepository
import com.vishalk.rssbstream.data.repository.MusicRepositoryImpl
import com.vishalk.rssbstream.data.repository.RemoteContentRepository
import com.vishalk.rssbstream.data.repository.RemoteContentRepositoryImpl
import com.vishalk.rssbstream.data.repository.TransitionRepository
import com.vishalk.rssbstream.data.repository.TransitionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): RssbStreamApplication {
        return app as RssbStreamApplication
    }

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Singleton
    @Provides
    fun provideJson(): Json { // Proveer Json
        return Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Singleton
    @Provides
    fun provideRssbStreamDatabase(@ApplicationContext context: Context): RssbStreamDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RssbStreamDatabase::class.java,
            "rssbstream_database"
        ).addMigrations(
            RssbStreamDatabase.MIGRATION_3_4,
            RssbStreamDatabase.MIGRATION_4_5,
            RssbStreamDatabase.MIGRATION_6_7
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideAlbumArtThemeDao(database: RssbStreamDatabase): AlbumArtThemeDao {
        return database.albumArtThemeDao()
    }

    @Singleton
    @Provides
    fun provideSearchHistoryDao(database: RssbStreamDatabase): SearchHistoryDao { // NUEVO MÉTODO
        return database.searchHistoryDao()
    }

    @Singleton
    @Provides
    fun provideMusicDao(database: RssbStreamDatabase): MusicDao { // Proveer MusicDao
        return database.musicDao()
    }

    @Singleton
    @Provides
    fun provideTransitionDao(database: RssbStreamDatabase): TransitionDao {
        return database.transitionDao()
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .dispatcher(Dispatchers.Default) // Use CPU-bound dispatcher for decoding
            .allowHardware(true) // Re-enable hardware bitmaps for better performance
            .build()
    }

    @Provides
    @Singleton
    fun provideLyricsRepository(
        @ApplicationContext context: Context,
        lrcLibApiService: LrcLibApiService,
        musicDao: MusicDao
    ): LyricsRepository {
        return LyricsRepositoryImpl(
            context = context,
            lrcLibApiService = lrcLibApiService,
            musicDao = musicDao
        )
    }

    @Provides
    @Singleton
    fun provideMusicRepository(
        @ApplicationContext context: Context,
        userPreferencesRepository: UserPreferencesRepository,
        searchHistoryDao: SearchHistoryDao,
        musicDao: MusicDao, // Añadir MusicDao como parámetro
        lyricsRepository: LyricsRepository
    ): MusicRepository {
        return MusicRepositoryImpl(
            context = context,
            userPreferencesRepository = userPreferencesRepository,
            searchHistoryDao = searchHistoryDao,
            musicDao = musicDao,
            lyricsRepository = lyricsRepository
        )
    }

    @Provides
    @Singleton
    fun provideTransitionRepository(
        transitionRepositoryImpl: TransitionRepositoryImpl
    ): TransitionRepository {
        return transitionRepositoryImpl
    }

    @Singleton
    @Provides
    fun provideSongMetadataEditor(@ApplicationContext context: Context, musicDao: MusicDao): SongMetadataEditor {
        return SongMetadataEditor(context, musicDao)
    }

    /**
     * Provee una instancia singleton de OkHttpClient con un interceptor de logging.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Provee una instancia singleton de Retrofit para la API de LRCLIB.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://lrclib.net/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provee una instancia singleton del servicio de la API de LRCLIB.
     */
    @Provides
    @Singleton
    fun provideLrcLibApiService(retrofit: Retrofit): LrcLibApiService {
        return retrofit.create(LrcLibApiService::class.java)
    }

    // ===== RSSB Content Streaming =====

    @Singleton
    @Provides
    fun provideRssbContentDao(database: RssbStreamDatabase): RssbContentDao {
        return database.rssbContentDao()
    }

    /**
     * Retrofit instance for R2 content catalog API.
     */
    @Provides
    @Singleton
    @Named("R2Retrofit")
    fun provideR2Retrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(R2Config.BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideContentCatalogApi(@Named("R2Retrofit") retrofit: Retrofit): ContentCatalogApi {
        return retrofit.create(ContentCatalogApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteContentRepository(
        @ApplicationContext context: Context,
        rssbContentDao: RssbContentDao,
        catalogApi: ContentCatalogApi,
        okHttpClient: OkHttpClient,
        preferencesManager: PreferencesManager
    ): RemoteContentRepository {
        return RemoteContentRepositoryImpl(
            context = context,
            contentDao = rssbContentDao,
            catalogApi = catalogApi,
            okHttpClient = okHttpClient,
            preferencesManager = preferencesManager
        )
    }
}