package io.cronit.service;

import io.cronit.builder.JobExecutionHistoryBuilder;
import io.cronit.common.Clock;
import io.cronit.domain.JobExecutionHistory;
import io.cronit.domain.JobExecutionStatus;
import io.cronit.repository.JobExecutionHistoryRepository;
import io.cronit.utils.ClockUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionHistoryRepositoryTest {

    @InjectMocks
    private JobExecutionHistoryService jobExecutionHistoryService;

    @Mock
    private JobExecutionHistoryRepository jobExecutionHistoryRepository;

    @Test
    public void it_should_insert_job_execution_when_job_started() {
        ArgumentCaptor<JobExecutionHistory> jobExecutionHistoryArgumentCaptor = ArgumentCaptor.forClass(JobExecutionHistory.class);
        Clock.freeze(ClockUtils.toLocalDate("20170315"));
        jobExecutionHistoryService.start("JobId");

        verify(jobExecutionHistoryRepository).save(jobExecutionHistoryArgumentCaptor.capture());

        JobExecutionHistory jobExecutionHistory = jobExecutionHistoryArgumentCaptor.getValue();

        assertThat(jobExecutionHistory.getStatus()).isEqualTo(JobExecutionStatus.Started);
        assertThat(jobExecutionHistory.getJobModelId()).isEqualTo("JobId");
        assertThat(jobExecutionHistory.getStartDate()).isEqualTo(Clock.now());
        Clock.unfreeze();
    }


    @Test
    public void it_should_update_job_execution_when_job_finished() {
        Clock.freeze(ClockUtils.toLocalDate("20170316"));
        DateTime startDate = DateTime.parse("20170315");

        JobExecutionHistory jobExecutionHistory = new JobExecutionHistoryBuilder().jobModelId("JobId").
                startDate(startDate).status(JobExecutionStatus.Started).build();

        when(jobExecutionHistoryRepository.findOne(jobExecutionHistory.getId())).thenReturn(jobExecutionHistory);

        jobExecutionHistoryService.update(jobExecutionHistory.getId(), JobExecutionStatus.Failed, "Error Message");

        verify(jobExecutionHistoryRepository).save(jobExecutionHistory);

        assertThat(jobExecutionHistory.getStatus()).isEqualTo(JobExecutionStatus.Failed);
        assertThat(jobExecutionHistory.getJobModelId()).isEqualTo("JobId");
        assertThat(jobExecutionHistory.getId()).isEqualTo(jobExecutionHistory.getId());
        assertThat(jobExecutionHistory.getEndDate()).isEqualTo(Clock.now());
        assertThat(jobExecutionHistory.getStartDate()).isEqualTo(startDate);
        assertThat(jobExecutionHistory.getErrorMessage()).isEqualTo("Error Message");
        Clock.unfreeze();
    }
}