package tasks.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tasks.model.Task;

import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("deprecation")
class NewEditControllerTest {


    private int startYear;
    private int startMonth;
    private int startDay;
    private NewEditController controller;
    private boolean repeated;
    private boolean isActive;
    private int interval;
    private Date startDate;
    private ObservableList<Task> tasksList;
    private Date validEndDate;


    private static String generateString(int length) {
        //noinspection SpellCheckingInspection
        String alphanumeric = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123546789 .;,-";

        StringBuilder string = new StringBuilder();
        while (string.length() != length)
            string.append(alphanumeric.charAt((int) (Math.random() * alphanumeric.length())));

        return string.toString();
    }

    @BeforeEach
    void setUp() {
        tasksList = FXCollections.observableArrayList();

        controller = new NewEditController();
        controller.setTasksList(tasksList);

        repeated = true;
        isActive = true;
        interval = 30;

        startYear = 2020;
        startMonth = Calendar.FEBRUARY;
        startDay = 12;
        startDate = new Date(startYear, startMonth, startDay, 0, 0);

        validEndDate = new Date(startYear, startMonth, startDay + 1, 0, 0);
    }

    @AfterEach
    void tearDown() {
        controller = null;
        tasksList = null;
        startDate = null;
        validEndDate = null;
    }

    @DisplayName("Should succeed if end date is not before start date when addTask is called.")
    @ParameterizedTest(name = "{0} days after start date.")
    @ValueSource(ints = {0, 1})
    void shouldSucceedIfEndDateNotBeforeStartDateWhenAddTask(int offset) {
        Date endDate = new Date(startYear, startMonth, startDay + offset, 0, 0);
        String description = "Desc1";

        controller.addTask(description, startDate, endDate, repeated, isActive, interval);

        Assertions.assertEquals(1, tasksList.size());
    }

    @DisplayName("Should fail if end date is before start date when addTask is called.")
    @ParameterizedTest(name = "{0} days before start date.")
    @ValueSource(ints = {1, 2})
    void shouldFailIfEndDateBeforeStartDateWhenAddTask(int offset) {
        Date endDate = new Date(startYear, startMonth, startDay - offset, 0, 0);
        String description = "Desc1";

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> controller.addTask(description, startDate, endDate, repeated, isActive, interval));
    }

    @DisplayName("Should succeed if description length is 1-255 when addTask is called.")
    @ParameterizedTest(name = "description length = {0}")
    @ValueSource(ints = {1, 254, 255})
    void shouldSucceedIfDescriptionLengthValidWhenAddTask(int length) {
        String description = generateString(length);

        controller.addTask(description, startDate, validEndDate, repeated, isActive, interval);

        Assertions.assertEquals(1, tasksList.size());
    }

    @DisplayName("Should fail if description length is 256 (>255) when addTask is called.")
    @Test
    void shouldFailIfDescriptionLengthTooLongWhenAddTask() {
        String description = generateString(256);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> controller.addTask(description, startDate, validEndDate, repeated, isActive, interval));
    }

    @DisplayName("Should fail if description is empty string when addTask is called.")
    @Test
    void shouldFailIfDescriptionEmptyWhenAddTask() {
        String description = generateString(0);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> controller.addTask(description, startDate, validEndDate, repeated, isActive, interval));
    }

    @DisplayName("Should fail if description is null when addTask is called.")
    @Test
    void shouldFailIfDescriptionNullWhenAddTask() {
        String description = null;

        Assertions.assertThrows(NullPointerException.class,
                () -> controller.addTask(description, startDate, validEndDate, repeated, isActive, interval));
    }
}
