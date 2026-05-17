package com.pollocontrol.app.domain.model

data class BatchSummary(
    val id: Long,
    val name: String,
    val entryDate: Long,
    val initialCount: Int,
    val breed: String,
    val shed: String,
    val status: String,
    val aliveCount: Int = 0,
    val totalMortality: Int = 0,
    val age: Int = 0,
    val totalCost: Double = 0.0,
    val costPerChicken: Double = 0.0,
    val feedCost: Double = 0.0
)
