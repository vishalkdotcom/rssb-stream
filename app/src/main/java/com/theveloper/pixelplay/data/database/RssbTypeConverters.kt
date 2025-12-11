package com.theveloper.pixelplay.data.database

import androidx.room.TypeConverter
import com.theveloper.pixelplay.data.model.ContentType

/**
 * Room type converters for RSSB content database.
 */
class RssbTypeConverters {
    
    @TypeConverter
    fun fromContentType(type: ContentType): String {
        return type.name
    }
    
    @TypeConverter
    fun toContentType(value: String): ContentType {
        return try {
            ContentType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ContentType.QNA // Default fallback
        }
    }
}
