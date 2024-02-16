package dev.bpmcrafters.example.javac7.application.port;

import java.util.Map;

public interface UserTaskOutPort {

  Map<String, Map<String, ?>> getAllTasks();
}
