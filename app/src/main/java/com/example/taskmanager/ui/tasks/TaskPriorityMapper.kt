package com.example.taskmanager.tasks

object TaskPriorityMapper {
    private val priorityMap = mapOf(0 to "Low", 1 to "Medium", 2 to "High")

    fun fromInt(priority: Int): String = priorityMap[priority] ?: "Low"

    fun toInt(priority: String): Int = priorityMap.entries.find { it.value == priority }?.key ?: 0
}