package com.nextbreakpoint.flinkoperator.controller.operation

import com.nextbreakpoint.flinkoperator.common.model.ClusterSelector
import com.nextbreakpoint.flinkoperator.common.model.FlinkAddress
import com.nextbreakpoint.flinkoperator.common.model.FlinkOptions
import com.nextbreakpoint.flinkoperator.common.model.SavepointInfo
import com.nextbreakpoint.flinkoperator.common.model.SavepointRequest
import com.nextbreakpoint.flinkoperator.common.utils.FlinkClient
import com.nextbreakpoint.flinkoperator.common.utils.KubeClient
import com.nextbreakpoint.flinkoperator.controller.core.OperationStatus
import com.nextbreakpoint.flinkoperator.testing.KotlinMockito.eq
import com.nextbreakpoint.flinkoperator.testing.KotlinMockito.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions

class SavepointQueryTest {
    private val clusterSelector = ClusterSelector(namespace = "flink", name = "test", uuid = "123")
    private val flinkOptions = FlinkOptions(hostname = "localhost", portForward = null, useNodePort = false)
    private val flinkClient = mock(FlinkClient::class.java)
    private val kubeClient = mock(KubeClient::class.java)
    private val flinkAddress = FlinkAddress(host = "localhost", port = 8080)
    private val command = SavepointQuery(flinkOptions, flinkClient, kubeClient)
    private val savepointRequest = SavepointRequest(jobId = "1", triggerId = "100")

    @BeforeEach
    fun configure() {
        given(kubeClient.findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))).thenReturn(flinkAddress)
        given(flinkClient.getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))).thenReturn(mapOf())
    }

    @Test
    fun `should fail when kubeClient throws exception`() {
        given(kubeClient.findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))).thenThrow(RuntimeException::class.java)
        val result = command.execute(clusterSelector, savepointRequest)
        verify(kubeClient, times(1)).findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))
        verifyNoMoreInteractions(kubeClient)
        verifyNoMoreInteractions(flinkClient)
        assertThat(result).isNotNull()
        assertThat(result.status).isEqualTo(OperationStatus.ERROR)
        assertThat(result.output).isNull()
    }

    @Test
    fun `should return expected result when there are pending requests`() {
        given(flinkClient.getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))).thenReturn(mapOf("1" to SavepointInfo("IN_PROGRESS", "")))
        val result = command.execute(clusterSelector, savepointRequest)
        verify(kubeClient, times(1)).findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))
        verify(flinkClient, times(1)).getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))
        verifyNoMoreInteractions(kubeClient)
        verifyNoMoreInteractions(flinkClient)
        assertThat(result).isNotNull()
        assertThat(result.status).isEqualTo(OperationStatus.OK)
        assertThat(result.output).isNull()
    }

    @Test
    fun `should return expected result when there are failed requests`() {
        given(flinkClient.getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))).thenReturn(mapOf("1" to SavepointInfo("FAILED", "")))
        val result = command.execute(clusterSelector, savepointRequest)
        verify(kubeClient, times(1)).findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))
        verify(flinkClient, times(1)).getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))
        verifyNoMoreInteractions(kubeClient)
        verifyNoMoreInteractions(flinkClient)
        assertThat(result).isNotNull()
        assertThat(result.status).isEqualTo(OperationStatus.ERROR)
        assertThat(result.output).isNull()
    }

    @Test
    fun `should return expected result when there are completed requests`() {
        given(flinkClient.getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))).thenReturn(mapOf("1" to SavepointInfo("COMPLETED", "file://tmp/000")))
        val result = command.execute(clusterSelector, savepointRequest)
        verify(kubeClient, times(1)).findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))
        verify(flinkClient, times(1)).getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))
        verifyNoMoreInteractions(kubeClient)
        verifyNoMoreInteractions(flinkClient)
        assertThat(result).isNotNull()
        assertThat(result.status).isEqualTo(OperationStatus.OK)
        assertThat(result.output).isEqualTo("file://tmp/000")
    }

    @Test
    fun `should return expected result when location is missing`() {
        given(flinkClient.getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))).thenReturn(mapOf("1" to SavepointInfo("COMPLETED", null)))
        val result = command.execute(clusterSelector, savepointRequest)
        verify(kubeClient, times(1)).findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))
        verify(flinkClient, times(1)).getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))
        verifyNoMoreInteractions(kubeClient)
        verifyNoMoreInteractions(flinkClient)
        assertThat(result).isNotNull()
        assertThat(result.status).isEqualTo(OperationStatus.ERROR)
        assertThat(result.output).isNull()
    }

    @Test
    fun `should return expected result when there are no completed requests`() {
        val result = command.execute(clusterSelector, savepointRequest)
        verify(kubeClient, times(1)).findFlinkAddress(eq(flinkOptions), eq("flink"), eq("test"))
        verify(flinkClient, times(1)).getSavepointRequestsStatus(eq(flinkAddress), eq(mapOf(savepointRequest.jobId to savepointRequest.triggerId)))
        verifyNoMoreInteractions(kubeClient)
        verifyNoMoreInteractions(flinkClient)
        assertThat(result).isNotNull()
        assertThat(result.status).isEqualTo(OperationStatus.ERROR)
        assertThat(result.output).isNull()
    }
}