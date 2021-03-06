package com.nextbreakpoint.flink.k8s.common

import com.nextbreakpoint.flink.common.RestartPolicy
import com.nextbreakpoint.flink.common.SavepointMode
import com.nextbreakpoint.flink.common.SavepointRequest
import com.nextbreakpoint.flink.testing.TestFactory
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.junit.jupiter.api.Test

class FlinkJobStatusTest {
    private val flinkJob = TestFactory.aFlinkJob(name = "test-test", namespace = "flink")

    @Test
    fun `should store savepoint path and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointTimestamp(flinkJob)).isEqualTo(DateTime(0))
        FlinkJobStatus.setSavepointPath(flinkJob, "/tmp/xxx")
        assertThat(FlinkJobStatus.getSavepointPath(flinkJob)).isEqualTo("/tmp/xxx")
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
        assertThat(FlinkJobStatus.getSavepointTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }

    @Test
    fun `should not store savepoint path and update timestamp if path is blank`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointTimestamp(flinkJob)).isEqualTo(DateTime(0))
        FlinkJobStatus.setSavepointPath(flinkJob, "")
        assertThat(FlinkJobStatus.getSavepointPath(flinkJob)).isNull()
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
        assertThat(FlinkJobStatus.getSavepointTimestamp(flinkJob)).isEqualTo(DateTime(0))
    }

    @Test
    fun `should store savepoint request and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointRequest(flinkJob)).isNull()
        FlinkJobStatus.setSavepointRequest(flinkJob, SavepointRequest("000", "XXX"))
        assertThat(FlinkJobStatus.getSavepointRequest(flinkJob)).isEqualTo(SavepointRequest("000", "XXX"))
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }

    @Test
    fun `should reset savepoint request and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isEqualTo(DateTime(0))
        FlinkJobStatus.resetSavepointRequest(flinkJob)
        assertThat(FlinkJobStatus.getSavepointRequest(flinkJob)).isNull()
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
        assertThat(FlinkJobStatus.getSavepointRequestTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }

    @Test
    fun `should store flink job digest and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getBootstrapDigest(flinkJob)).isNull()
        FlinkJobStatus.setBootstrapDigest(flinkJob, "XXX")
        assertThat(FlinkJobStatus.getBootstrapDigest(flinkJob)).isEqualTo("XXX")
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }

    @Test
    fun `should store job parallelism and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getJobParallelism(flinkJob)).isEqualTo(0)
        FlinkJobStatus.setJobParallelism(flinkJob, 4)
        assertThat(FlinkJobStatus.getJobParallelism(flinkJob)).isEqualTo(4)
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }

    @Test
    fun `should store savepoint mode and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getSavepointMode(flinkJob)).isEqualTo(SavepointMode.Automatic)
        FlinkJobStatus.setSavepointMode(flinkJob, SavepointMode.Manual)
        assertThat(FlinkJobStatus.getSavepointMode(flinkJob)).isEqualTo(SavepointMode.Manual)
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }

    @Test
    fun `should store job restart policy and update timestamp`() {
        val timestamp = DateTime(System.currentTimeMillis())
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isEqualTo(DateTime(0))
        assertThat(FlinkJobStatus.getRestartPolicy(flinkJob)).isEqualTo(RestartPolicy.Always)
        FlinkJobStatus.setRestartPolicy(flinkJob, RestartPolicy.Never)
        assertThat(FlinkJobStatus.getRestartPolicy(flinkJob)).isEqualTo(RestartPolicy.Never)
        assertThat(FlinkJobStatus.getStatusTimestamp(flinkJob)).isGreaterThanOrEqualTo(timestamp)
    }
}
