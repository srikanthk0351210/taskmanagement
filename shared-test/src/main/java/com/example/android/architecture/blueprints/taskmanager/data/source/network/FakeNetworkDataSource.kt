
package com.example.taskmanager.data.source.network

class FakeNetworkDataSource(
    var tasks: MutableList<NetworkTask>? = mutableListOf()
) : NetworkDataSource {
    override suspend fun loadTasks() = tasks ?: throw Exception("Task list is null")

    override suspend fun saveTasks(tasks: List<NetworkTask>) {
        this.tasks = tasks.toMutableList()
    }
}
