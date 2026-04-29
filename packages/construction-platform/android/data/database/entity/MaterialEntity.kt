package com.constructionmanager.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.constructionmanager.domain.model.MaterialCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: MaterialCategory,
    val subcategory: String,
    val unitOfMeasurement: String,
    val currentPrice: String, // BigDecimal as string
    val supplier: String,
    val supplierSku: String? = null,
    val description: String,
    val specifications: String, // JSON string of Map<String, String>
    val isActive: Boolean = true,
    val lastPriceUpdate: String, // ISO datetime string
    val regionalPricing: String = "{}" // JSON string of Map<String, BigDecimal>
)

class DatabaseConverters {
    @TypeConverter
    fun fromSpecificationsMap(value: Map<String, String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toSpecificationsMap(value: String): Map<String, String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromRegionalPricingMap(value: Map<String, BigDecimal>): String {
        val stringMap = value.mapValues { it.value.toString() }
        return Json.encodeToString(stringMap)
    }

    @TypeConverter
    fun toRegionalPricingMap(value: String): Map<String, BigDecimal> {
        val stringMap: Map<String, String> = Json.decodeFromString(value)
        return stringMap.mapValues { BigDecimal(it.value) }
    }
}