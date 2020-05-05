package tasks.model;

import java.io.Serializable;
import java.util.List;

public interface TaskList extends Iterable<Task>, Serializable  {
    void add(Task task);
    boolean remove(Task task);
    int size();
    Task getTask(int index);
    List<Task> getAll();

}
