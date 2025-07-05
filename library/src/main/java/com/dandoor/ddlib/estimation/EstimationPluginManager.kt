package com.dandoor.ddlib.estimation

import com.dandoor.ddlib.data.entity.EstiData
import com.dandoor.ddlib.data.entity.EstimationSummary
import com.dandoor.ddlib.data.entity.Position
import com.dandoor.ddlib.data.entity.config
import com.dandoor.ddlib.estimation.plugin.EstimationPlugin
import com.dandoor.ddlib.estimation.model.TimeWindowBeaconRssi
import com.dandoor.ddlib.repository.DataManager
import com.dandoor.ddlib.repository.DataManager.Companion.defualtConfig

class EstimationPluginManager(
    private val dtManager: DataManager
) {
    private val plugins = mutableListOf<EstimationPlugin>()

    fun register(plugin: EstimationPlugin) {
        plugins.add(plugin)
    }

    fun unregister(plugin: EstimationPlugin) {
        plugins.remove(plugin)
    }

    fun calcEstiPos(input: TimeWindowBeaconRssi, pluginName: String = "Trilateration"): Position? {
        val plugin = plugins.find { it.name == pluginName }
        return plugin?.calcEstiPos(input)
    }

    // 여러 윈도우 데이터에 대해 일괄 평가
    fun calc(
        input: List<TimeWindowBeaconRssi>,
        config: config,
        pluginName: String = "Trilateration"
    ): List<EstimationSummary> {
        val plugin = plugins.find { it.name == pluginName }
            ?: throw IllegalArgumentException("Plugin $pluginName not registered")
        return plugin.calc(input, config)
    }

    /** 이거 쓰면 됨..! */
    fun calcAndSave(
        labId: Long,
        input: List<TimeWindowBeaconRssi>,
        config: config = defualtConfig,
        pluginName: String = "Trilateration"
    ) {
        val plugin = plugins.find { it.name == pluginName } ?: throw IllegalArgumentException("Plugin $pluginName not registered")
        val results = plugin.calc(input, config)
        val estiDataList = results.map { summary ->
            EstiData(
                timestamp = summary.timestamp,
                esti_pos = summary.estiPos,
                real_pos = summary.realPos,
                error = summary.error,
                lid = labId,
                method = plugin.name
            )
        }

        dtManager.saveAllEstiDataSync(estiDataList)
    }

    fun getAvailablePlugins(): List<String> = plugins.map { it.name }
}
