package com.theawesomeengineer.taskmanager.service;

import com.theawesomeengineer.taskmanager.domain.Task;
import com.theawesomeengineer.taskmanager.repo.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    @Test
    void create_shouldSaveTask() {
        Task task = new Task();
        task.setTitle("Test task");

        when(repository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task t = invocation.getArgument(0);
                    t.setId(1L);
                    return t;
                });

        Task created = service.create(task);

        assertNotNull(created.getId());
        verify(repository).save(any(Task.class));
    }
}
