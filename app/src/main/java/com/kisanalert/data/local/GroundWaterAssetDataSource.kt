package com.kisanalert.data.local

import android.content.Context
import com.kisanalert.domain.model.GroundWaterCategory
import com.kisanalert.domain.model.GroundWaterDistrictAssessment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class GroundWaterLookupRecord(
    val state: String,
    val district: String,
    val districtAliases: List<String>,
    val assessment: GroundWaterDistrictAssessment
)

@Serializable
private data class GroundWaterDistrictRecord(
    val state: String,
    val district: String,
    @SerialName("stageOfExtractionPercent")
    val stageOfExtractionPercent: Double,
    val category: String,
    @SerialName("annualExtractableResourceHam")
    val annualExtractableResourceHam: Double,
    @SerialName("totalAnnualExtractionHam")
    val totalAnnualExtractionHam: Double,
    @SerialName("netAvailabilityForFutureHam")
    val netAvailabilityForFutureHam: Double,
    @SerialName("totalAnnualRechargeHam")
    val totalAnnualRechargeHam: Double,
    @SerialName("assessmentYear")
    val assessmentYear: String,
    val source: String,
    @SerialName("districtAliases")
    val districtAliases: List<String> = emptyList()
)

@Singleton
class GroundWaterAssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
    @Volatile
    private var cachedRecords: List<GroundWaterDistrictRecord>? = null

    suspend fun loadLookupRecords(): List<GroundWaterLookupRecord> {
        val records: List<GroundWaterDistrictRecord> = getCachedRecords()
        return records.map { record ->
            GroundWaterLookupRecord(
                state = record.state,
                district = record.district,
                districtAliases = record.districtAliases,
                assessment = record.toDomain()
            )
        }
    }

    private fun getCachedRecords(): List<GroundWaterDistrictRecord> {
        val existingRecords: List<GroundWaterDistrictRecord>? = cachedRecords
        if (existingRecords != null) {
            return existingRecords
        }
        val loadedRecords: List<GroundWaterDistrictRecord> = readRecordsFromAssets()
        cachedRecords = loadedRecords
        return loadedRecords
    }

    private fun readRecordsFromAssets(): List<GroundWaterDistrictRecord> {
        val jsonText: String = context.assets
            .open(GROUNDWATER_ASSET_PATH)
            .bufferedReader()
            .use { reader -> reader.readText() }
        return json.decodeFromString<List<GroundWaterDistrictRecord>>(jsonText)
    }

    private fun GroundWaterDistrictRecord.toDomain(): GroundWaterDistrictAssessment {
        return GroundWaterDistrictAssessment(
            state = state,
            district = district,
            stageOfExtractionPercent = stageOfExtractionPercent,
            category = parseCategory(),
            annualExtractableResourceHam = annualExtractableResourceHam,
            totalAnnualExtractionHam = totalAnnualExtractionHam,
            netAvailabilityForFutureHam = netAvailabilityForFutureHam,
            totalAnnualRechargeHam = totalAnnualRechargeHam,
            assessmentYear = assessmentYear,
            source = source
        )
    }

    private fun GroundWaterDistrictRecord.parseCategory(): GroundWaterCategory {
        return when (category.uppercase()) {
            "SAFE" -> GroundWaterCategory.SAFE
            "SEMI_CRITICAL" -> GroundWaterCategory.SEMI_CRITICAL
            "CRITICAL" -> GroundWaterCategory.CRITICAL
            "OVER_EXPLOITED" -> GroundWaterCategory.OVER_EXPLOITED
            else -> GroundWaterCategory.fromStagePercent(stageOfExtractionPercent)
        }
    }

    companion object {
        private const val GROUNDWATER_ASSET_PATH: String = "groundwater/groundwater_districts.json"
    }
}
