package tasks.repository;

import javafx.collections.ObservableList;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tasks.model.Task;
import tasks.model.TaskList;

import java.io.*;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskRepositoryTest {

    @Mock
    Task task;

    @Mock
    Iterator<Task> iterator;
    @Mock
    ObservableList<Task> tasksList;

    @Spy
    @InjectMocks
    TaskRepository taskRepository;

    @Mock
    private Appender appender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;


    @BeforeEach
    public void setUp() {
        initMocks(this);

        Logger root = Logger.getLogger(TaskRepository.class.getName());
        root.addAppender(appender);
        root.setLevel(Level.INFO);
    }

    @Test
    public void shouldSucceedWhenRewriteFileIfExpectedBehaviour() throws IOException {

        when(tasksList.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(task);
        doNothing().when(taskRepository).writeBinary(any(TaskList.class), any(File.class));

        assertDoesNotThrow(() -> taskRepository.rewriteFile(tasksList));

        verify(tasksList, times(1)).iterator();
        verify(iterator, atLeastOnce()).hasNext();
        verify(iterator, atLeastOnce()).next();
        verify(taskRepository, times(1)).writeBinary(any(TaskList.class), any(File.class));
        verify(appender, times(0)).doAppend(loggingEventCaptor.capture());
    }

    @Test
    public void shouldFailWhenRewriteFileIfIOException() throws IOException {

        when(tasksList.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(task);
        doThrow(IOException.class).when(taskRepository).writeBinary(any(TaskList.class), any(File.class));

        assertDoesNotThrow(() -> taskRepository.rewriteFile(tasksList));

        verify(tasksList, times(1)).iterator();
        verify(iterator, atLeastOnce()).hasNext();
        verify(iterator, atLeastOnce()).next();
        verify(taskRepository, times(1)).writeBinary(any(TaskList.class), any(File.class));
        verify(appender, times(1)).doAppend(loggingEventCaptor.capture());

        LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);

        assertEquals(TaskRepository.ERROR_MESSAGE, loggingEvent.getMessage());
        assertEquals(Level.ERROR, loggingEvent.getLevel());
    }

}
