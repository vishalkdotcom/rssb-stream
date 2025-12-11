package com.theveloper.pixelplay.data.network

import com.theveloper.pixelplay.data.model.remote.AudiobookCatalogItem
import com.theveloper.pixelplay.data.model.remote.DiscourseCatalogItem
import com.theveloper.pixelplay.data.model.remote.QnaCatalogItem
import com.theveloper.pixelplay.data.model.remote.ShabadCatalogItem
import retrofit2.http.GET

/**
 * Retrofit API service for fetching content catalogs from R2.
 */
interface ContentCatalogApi {
    
    @GET("catalog/audiobooks.json")
    suspend fun getAudiobooks(): List<AudiobookCatalogItem>
    
    @GET("catalog/qna.json")
    suspend fun getQnaSessions(): List<QnaCatalogItem>
    
    @GET("catalog/shabads.json")
    suspend fun getShabads(): List<ShabadCatalogItem>
    
    @GET("catalog/discourses.json")
    suspend fun getDiscourses(): List<DiscourseCatalogItem>
}
