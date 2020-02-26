package com.nextbreakpoint.flinkoperator.controller.operation

import com.nextbreakpoint.flinkoperator.common.crd.V1BootstrapSpec
import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.ClusterStatus
import com.nextbreakpoint.flinkoperator.common.model.ClusterTask
import com.nextbreakpoint.flinkoperator.common.model.FlinkOptions
import com.nextbreakpoint.flinkoperator.common.model.StartOptions
import com.nextbreakpoint.flinkoperator.common.utils.FlinkClient
import com.nextbreakpoint.flinkoperator.common.utils.KubeClient
import com.nextbreakpoint.flinkoperator.controller.core.CacheAdapter
import com.nextbreakpoint.flinkoperator.controller.core.Operation
import com.nextbreakpoint.flinkoperator.controller.core.OperationResult
import com.nextbreakpoint.flinkoperator.controller.core.OperationStatus
import org.apache.log4j.Logger

class ClusterStart(flinkOptions: FlinkOptions, flinkClient: FlinkClient, kubeClient: KubeClient, private val adapter: CacheAdapter) : Operation<StartOptions, List<ClusterTask>>(flinkOptions, flinkClient, kubeClient) {
    companion object {
        private val logger = Logger.getLogger(ClusterStart::class.simpleName)
    }

    override fun execute(clusterId: ClusterId, params: StartOptions): OperationResult<List<ClusterTask>> {
        try {
            val statusList = tryStartingCluster(adapter.getBootstrap(), adapter.getClusterStatus(), params)

            if (statusList.isEmpty()) {
                logger.warn("[name=${clusterId.name}] Can't change tasks sequence")

                return OperationResult(
                    OperationStatus.RETRY,
                    listOf()
                )
            }

            adapter.appendTasks(statusList)

            return OperationResult(
                OperationStatus.COMPLETED,
                statusList
            )
        } catch (e : Exception) {
            logger.error("[name=${clusterId.name}] Can't change tasks sequence", e)

            return OperationResult(
                OperationStatus.FAILED,
                listOf()
            )
        }
    }

    private fun tryStartingCluster(bootstrapSpec: V1BootstrapSpec?, clusterStatus: ClusterStatus, params: StartOptions): List<ClusterTask> {
        return if (bootstrapSpec == null) {
            when (clusterStatus) {
                ClusterStatus.Terminated ->
                    listOf(
                        ClusterTask.StartingCluster,
                        ClusterTask.CreateResources,
                        ClusterTask.ClusterRunning
                    )
                ClusterStatus.Suspended ->
                    listOf(
                        ClusterTask.StartingCluster,
                        ClusterTask.RestartPods,
                        ClusterTask.ClusterRunning
                    )
                ClusterStatus.Failed ->
                    listOf(
                        ClusterTask.StoppingCluster,
                        ClusterTask.TerminatePods,
                        ClusterTask.StartingCluster,
                        ClusterTask.CreateResources,
                        ClusterTask.ClusterRunning
                    )
                else -> listOf()
            }
        } else {
            when (clusterStatus) {
                ClusterStatus.Terminated ->
                    if (params.withoutSavepoint) {
                        listOf(
                            ClusterTask.StartingCluster,
                            ClusterTask.EraseSavepoint,
                            ClusterTask.CreateResources,
                            ClusterTask.CreateBootstrapJob,
                            ClusterTask.ClusterRunning
                        )
                    } else {
                        listOf(
                            ClusterTask.StartingCluster,
                            ClusterTask.CreateResources,
                            ClusterTask.CreateBootstrapJob,
                            ClusterTask.ClusterRunning
                        )
                    }
                ClusterStatus.Suspended ->
                    if (params.withoutSavepoint) {
                        listOf(
                            ClusterTask.StartingCluster,
                            ClusterTask.EraseSavepoint,
                            ClusterTask.RestartPods,
                            ClusterTask.CreateBootstrapJob,
                            ClusterTask.ClusterRunning
                        )
                    } else {
                        listOf(
                            ClusterTask.StartingCluster,
                            ClusterTask.RestartPods,
                            ClusterTask.CreateBootstrapJob,
                            ClusterTask.ClusterRunning
                        )
                    }
                ClusterStatus.Failed ->
                    if (params.withoutSavepoint) {
                        listOf(
                            ClusterTask.StoppingCluster,
                            ClusterTask.TerminatePods,
                            ClusterTask.StartingCluster,
                            ClusterTask.EraseSavepoint,
                            ClusterTask.CreateResources,
                            ClusterTask.CreateBootstrapJob,
                            ClusterTask.ClusterRunning
                        )
                    } else {
                        listOf(
                            ClusterTask.StoppingCluster,
                            ClusterTask.TerminatePods,
                            ClusterTask.StartingCluster,
                            ClusterTask.CreateResources,
                            ClusterTask.CreateBootstrapJob,
                            ClusterTask.ClusterRunning
                        )
                    }
                else -> listOf()
            }
        }
    }
}