package com.nextbreakpoint.flinkoperator.controller.core

import com.nextbreakpoint.flinkoperator.common.crd.V1FlinkCluster
import com.nextbreakpoint.flinkoperator.common.model.ClusterId
import com.nextbreakpoint.flinkoperator.common.model.Result
import com.nextbreakpoint.flinkoperator.common.model.ResultStatus
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResources
import com.nextbreakpoint.flinkoperator.controller.resources.ClusterResourcesBuilder
import com.nextbreakpoint.flinkoperator.controller.resources.DefaultClusterResourcesFactory

interface Task {
    fun onExecuting(context: TaskContext): Result<String>

    fun onAwaiting(context: TaskContext): Result<String>

    fun onIdle(context: TaskContext): Result<String>

    fun onFailed(context: TaskContext): Result<String>

    fun isBootstrapJobDefined(cluster: V1FlinkCluster) = cluster.spec?.bootstrap != null

    fun taskCompletedWithOutput(cluster: V1FlinkCluster, output: String): Result<String> =
        Result(ResultStatus.SUCCESS, "[name=${cluster.metadata.name}] $output")

    fun taskAwaitingWithOutput(cluster: V1FlinkCluster, output: String): Result<String> =
        Result(ResultStatus.AWAIT, "[name=${cluster.metadata.name}] $output")

    fun taskFailedWithOutput(cluster: V1FlinkCluster, output: String): Result<String> =
        Result(ResultStatus.FAILED, "[name=${cluster.metadata.name}] $output")

    fun createClusterResources(clusterId: ClusterId, cluster: V1FlinkCluster): ClusterResources {
        return ClusterResourcesBuilder(
            DefaultClusterResourcesFactory, clusterId.namespace, clusterId.uuid, "flink-operator", cluster
        ).build()
    }
}