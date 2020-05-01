package tasks.services;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.model.ArrayTaskList;
import tasks.model.Task;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TasksServiceTest {


    ArrayTaskList tasks;
    TasksService service;

    Date first;
    Date middle;
    Date last;

    @SuppressWarnings("deprecation")
    @BeforeEach
    void setUp() {
        first = new Date(2020, Calendar.FEBRUARY, 12, 10, 30);
        middle = new Date(2020, Calendar.FEBRUARY, 13, 10, 30);
        last = new Date(2020, Calendar.FEBRUARY, 14, 10, 30);

        tasks = new ArrayTaskList();

        tasks.add(new Task("f1", first));
        tasks.add(new Task("f2", middle));
        tasks.add(new Task("f3", last));

        tasks.add(new Task("i1", first, middle, 30));
        tasks.add(new Task("i2", middle, last, 30));
        tasks.add(new Task("i3", first, last, 30));

        tasks.getAll().forEach(task -> task.setActive(true));

        service = new TasksService(tasks);
    }

    @Test
    void WBT_TC_01() {
        Collection<Task> filtered = (Collection<Task>) service.filterTasks(first, middle);

        Assertions.assertEquals(
                Stream.of("f1", "f2", "i1", "i2", "i3")
                        .collect(Collectors.toSet()),
                filtered.stream()
                        .map(Task::getDescription)
                        .collect(Collectors.toSet()));
    }

    @Test
    void WBT_TC_02() {
        Collection<Task> filtered = (Collection<Task>) service.filterTasks(middle, last);

        Assertions.assertEquals(
                Stream.of("f2", "f3", "i1", "i2", "i3")
                        .collect(Collectors.toSet()),
                filtered.stream()
                        .map(Task::getDescription)
                        .collect(Collectors.toSet()));
    }

}