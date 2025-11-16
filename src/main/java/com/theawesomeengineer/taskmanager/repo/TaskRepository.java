package com.theawesomeengineer.taskmanager.repo;

import com.theawesomeengineer.taskmanager.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Additional query methods later if needed
}
