package com.theawesomeengineer.taskmanager.api;

import com.theawesomeengineer.taskmanager.domain.Task;
import com.theawesomeengineer.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());
        Task created = service.create(task);
        return ResponseEntity
                .created(URI.create("/tasks/" + created.getId()))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        Task updated = new Task();
        updated.setTitle(request.getTitle());
        updated.setDescription(request.getDescription());
        updated.setCompleted(request.isCompleted());

        Task result = service.update(id, updated);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
