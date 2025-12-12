package com.vishalk.rssbstream.data.network

/**
 * Configuration for Cloudflare R2 content streaming.
 * Update BASE_URL with your R2 bucket's public URL.
 */
object R2Config {
    /**
     * Base URL for your Cloudflare R2 bucket.
     * 
     * After setting up R2:
     * - If using r2.dev subdomain: "https://pub-{YOUR_ACCOUNT_ID}.r2.dev"
     * - If using custom domain: "https://stream.yourdomain.com"
     */
    const val BASE_URL = "https://pub-27aae3aab5364431a6558ffc550e23d4.r2.dev"
    
    // Catalog endpoints (JSON metadata files)
    const val AUDIOBOOKS_CATALOG = "$BASE_URL/catalog/audiobooks.json"
    const val QNA_CATALOG = "$BASE_URL/catalog/qna.json"
    const val SHABADS_CATALOG = "$BASE_URL/catalog/shabads.json"
    const val DISCOURSES_CATALOG = "$BASE_URL/catalog/discourses.json"
    
    // Audio base paths
    const val AUDIO_AUDIOBOOKS = "$BASE_URL/audio/audiobooks"
    const val AUDIO_QNA = "$BASE_URL/audio/qna"
    const val AUDIO_SHABADS = "$BASE_URL/audio/shabads"
    const val AUDIO_DISCOURSES = "$BASE_URL/audio/discourses"
    
    // Thumbnails
    const val THUMBNAILS = "$BASE_URL/thumbnails"
}
