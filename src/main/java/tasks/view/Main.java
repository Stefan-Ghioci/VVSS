package tasks.view;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import tasks.controller.Controller;
import tasks.services.NotificationHandler;
import tasks.model.ArrayTaskList;
import tasks.repository.TaskRepository;
import tasks.services.TasksService;

import java.io.File;
import java.io.IOException;


public class Main extends Application {
    private static Stage primaryStage;
    private static final int DEFAULT_WIDTH = 820;
    private static final int DEFAULT_HEIGHT = 520;

    private static final Logger log = Logger.getLogger(Main.class.getName());

    private ArrayTaskList savedTasksList = new ArrayTaskList();

    private static ClassLoader classLoader = Main.class.getClassLoader();
    private static File savedTasksFile = new File(classLoader.getResource("data/tasks.txt").getFile());

    private TasksService service = new TasksService(savedTasksList);
    private TaskRepository taskRepository = TaskRepository.getInstance();

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static File getSavedTasksFile() {
        return savedTasksFile;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        log.info("saved data reading");
        if (getSavedTasksFile().length() != 0) {
            taskRepository.readBinary(savedTasksList, getSavedTasksFile());
        }
        try {
            log.info("application start");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            Controller ctrl= loader.getController();
            service = new TasksService(savedTasksList);

            ctrl.setService(service);
            primaryStage.setTitle("Task Manager");
            primaryStage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
            primaryStage.setMinWidth(DEFAULT_WIDTH);
            primaryStage.setMinHeight(DEFAULT_HEIGHT);
            primaryStage.show();
        }
        catch (IOException e){
            log.error(e);
            log.error("error reading main.fxml");
        }
        primaryStage.setOnCloseRequest(we -> System.exit(0));
        new NotificationHandler(FXCollections.observableArrayList(service.getObservableList())).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
