package tasks.controller;

import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import tasks.model.Task;
import tasks.repository.TaskRepository;

import javax.swing.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ControllerTest {

    @Mock
    Task toDelete;

    @Mock
    TableView.TableViewSelectionModel<Task> model;

    @Mock
    TableView tasks;

    @Mock
    ObservableList<Task> taskList;

    @Mock
    TaskRepository repository;

    @InjectMocks
    Controller controller;


    @BeforeEach
    public void setUp() {
        initMocks(this);
    }

    @BeforeAll
    public static void initToolkit()
            throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            new JFXPanel(); // initializes JavaFX environment
            latch.countDown();
        });

        // That's a pretty reasonable delay... Right?
        if (!latch.await(50L, TimeUnit.SECONDS))
            throw new ExceptionInInitializerError();
    }


    @Test
    public void shouldSucceedWhenDeleteTaskIfExpectedBehaviour() {

        given(tasks.getSelectionModel()).willReturn(model);
        given(model.getSelectedItem()).willReturn(toDelete);
        given(taskList.remove(toDelete)).willReturn(true);
        doNothing().when(repository).rewriteFile(taskList);

        assertDoesNotThrow(()->controller.deleteTask());

        verify(tasks, times(1)).getSelectionModel();
        verify(model, times(1)).getSelectedItem();
        verify(taskList, times(1)).remove(toDelete);
        verify(repository, times(1)).rewriteFile(taskList);
    }

    @Test
    public void shouldFailWhenDeleteTaskIfRepositoryBreaks() {

        given(tasks.getSelectionModel()).willReturn(model);
        given(model.getSelectedItem()).willReturn(toDelete);
        given(taskList.remove(toDelete)).willReturn(true);
        doThrow(NullPointerException.class).when(repository).rewriteFile(taskList);

        assertThrows(NullPointerException.class, () -> controller.deleteTask());

        verify(tasks, times(1)).getSelectionModel();
        verify(model, times(1)).getSelectedItem();
        verify(taskList, times(1)).remove(toDelete);
        verify(repository, times(1)).rewriteFile(taskList);
    }
}
