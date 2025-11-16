package com.theawesomeengineer.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndGetTask() throws Exception {
        String json = """
            {
              "title": "My task",
              "description": "Test",
              "completed": false
            }
        """;

        // Create
        var result = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");

        // Get by location
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My task"));
    }

    @Test
    void createTask_missingTitle_returns400() throws Exception {
        String json = """
        {
          "description": "Missing title should trigger validation"
        }
        """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getTask_notFound_returns404() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id 9999 not found"));
    }


}
