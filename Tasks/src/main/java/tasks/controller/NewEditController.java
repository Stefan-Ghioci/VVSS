package tasks.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import tasks.model.Task;
import tasks.repository.TaskRepository;
import tasks.services.DateService;
import tasks.services.TasksService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;


public class NewEditController {

    private static Button clickedButton;

    private static final Logger log = Logger.getLogger(NewEditController.class.getName());
    private String buttonType;

    public static void setClickedButton(Button clickedButton) {
        NewEditController.clickedButton = clickedButton;
    }

    public static void setCurrentStage(Stage currentStage) {
        NewEditController.currentStage = currentStage;
    }

    private static Stage currentStage;

    private Task currentTask;
    private ObservableList<Task> tasksList;
    private TasksService service;
    private DateService dateService;


    @FXML
    private TextField fieldDescription;
    @FXML
    private DatePicker datePickerStart;
    @FXML
    private TextField txtFieldTimeStart;
    @FXML
    private DatePicker datePickerEnd;
    @FXML
    private TextField txtFieldTimeEnd;
    @FXML
    private TextField fieldInterval;
    @FXML
    private CheckBox checkBoxActive;
    @FXML
    private CheckBox checkBoxRepeated;

    private static final String DEFAULT_START_TIME = "8:00";
    private static final String DEFAULT_END_TIME = "10:00";
    private static final String DEFAULT_INTERVAL_TIME = "0:30";

    public void setTasksList(ObservableList<Task> tasksList) {
        this.tasksList = tasksList;
    }

    public void setService(TasksService service) {
        this.service = service;
        this.dateService = new DateService(service);
    }

    public void setCurrentTask(Task task) {
        this.currentTask = task;
        buttonType = clickedButton.getId();
        switch (buttonType) {
            case "btnNew":
                initNewWindow("New Task");
                break;
            case "btnEdit":
                initEditWindow("Edit Task");
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + buttonType);
        }
    }

    @FXML
    public void initialize() {
        log.info("new/edit window initializing");
    }

    private void initNewWindow(String title) {
        currentStage.setTitle(title);
        datePickerStart.setValue(LocalDate.now());
        txtFieldTimeStart.setText(DEFAULT_START_TIME);
    }

    private void initEditWindow(String title) {
        currentStage.setTitle(title);
        fieldDescription.setText(currentTask.getDescription());
        datePickerStart.setValue(DateService.getLocalDateValueFromDate(currentTask.getStartTime()));
        txtFieldTimeStart.setText(dateService.getTimeOfTheDayFromDate(currentTask.getStartTime()));

        if (currentTask.isRepeated()) {
            checkBoxRepeated.setSelected(true);
            hideRepeatedTaskModule(false);
            datePickerEnd.setValue(DateService.getLocalDateValueFromDate(currentTask.getEndTime()));
            fieldInterval.setText(service.getIntervalInHours(currentTask));
            txtFieldTimeEnd.setText(dateService.getTimeOfTheDayFromDate(currentTask.getEndTime()));
        }
        if (currentTask.isActive()) {
            checkBoxActive.setSelected(true);

        }
    }

    @FXML
    public void switchRepeatedCheckbox(ActionEvent actionEvent) {
        CheckBox source = (CheckBox) actionEvent.getSource();
        if (source.isSelected()) {
            hideRepeatedTaskModule(false);
        } else if (!source.isSelected()) {
            hideRepeatedTaskModule(true);
        }
    }

    private void hideRepeatedTaskModule(boolean toShow) {
        datePickerEnd.setDisable(toShow);
        fieldInterval.setDisable(toShow);
        txtFieldTimeEnd.setDisable(toShow);

        datePickerEnd.setValue(LocalDate.now());
        txtFieldTimeEnd.setText(DEFAULT_END_TIME);
        fieldInterval.setText(DEFAULT_INTERVAL_TIME);
    }

    @FXML
    public void saveChanges() {

        String newDescription = fieldDescription.getText();
        Date startDateWithNoTime = dateService.getDateValueFromLocalDate(datePickerStart.getValue());//ONLY date!!without time
        Date newStartDate = dateService.getDateMergedWithTime(txtFieldTimeStart.getText(), startDateWithNoTime);
        Date endDateWithNoTime = datePickerEnd.getValue() == null ? null : dateService.getDateValueFromLocalDate(datePickerEnd.getValue());
        Date newEndDate = endDateWithNoTime == null ? null : dateService.getDateMergedWithTime(txtFieldTimeEnd.getText(), endDateWithNoTime);
        boolean repeated = checkBoxRepeated.isSelected();
        boolean isActive = checkBoxActive.isSelected();
        int newInterval = fieldInterval.getText().equals("") ? 0 : service.parseFromStringToSeconds(fieldInterval.getText());


        try {

            if (buttonType.equals("btnNew")) {
                addTask(newDescription, newStartDate, newEndDate, repeated, isActive, newInterval);
            } else {
                updateTask(newDescription, newStartDate, newEndDate, repeated, isActive, newInterval);
            }
            Controller.editNewStage.close();
        } catch (RuntimeException e) {
            try {
                Stage stage = new Stage();
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/field-validator.fxml"));
                stage.setScene(new Scene(root, 350, 150));
                stage.setResizable(false);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ioe) {
                log.error("error loading field-validator.fxml");
            }
        }
    }

    public void updateTask(String description, Date startDate, Date endDate, boolean repeated, boolean isActive, int interval) {
        Task collectedFieldsTask = validateTask(description, startDate, endDate, repeated, isActive, interval);
        for (int i = 0; i < tasksList.size(); i++) {
            if (currentTask.equals(tasksList.get(i))) {
                tasksList.set(i, collectedFieldsTask);
                TaskRepository.rewriteFile(tasksList);
            }
        }
        currentTask = null;
    }

    public void addTask(String description, Date startDate, Date endDate, boolean repeated, boolean isActive, int interval) {
        Task collectedFieldsTask = validateTask(description, startDate, endDate, repeated, isActive, interval);
        tasksList.add(collectedFieldsTask);
        TaskRepository.rewriteFile(tasksList);
    }

    private Task validateTask(String newDescription, Date newStartDate, Date newEndDate, boolean repeated, boolean isActive, int newInterval) {
        Task collectedFieldsTask;
        if (repeated) {
            if (newStartDate.after(newEndDate))
                throw new IllegalArgumentException("Start date should be before end");
            collectedFieldsTask = new Task(newDescription, newStartDate, newEndDate, newInterval);
        } else {
            collectedFieldsTask = new Task(newDescription, newStartDate);
        }
        collectedFieldsTask.setActive(isActive);
        log.info(collectedFieldsTask);
        return collectedFieldsTask;
    }

    @FXML
    public void closeDialogWindow() {
        Controller.editNewStage.close();
    }

}
