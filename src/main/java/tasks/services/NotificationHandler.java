package tasks.services;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.controlsfx.control.Notifications;
import tasks.model.Task;

import java.util.Date;

public class NotificationHandler extends Thread {

    private static final int MILLISECONDS_IN_SEC = 1000;
    private static final int SECONDS_IN_MIN = 60;

    private static final Logger log = Logger.getLogger(NotificationHandler.class.getName());

    private ObservableList<Task> tasksList;

    public NotificationHandler(ObservableList<Task> tasksList){
        this.tasksList=tasksList;
    }

    @Override
    public void run() {
        Date currentDate = new Date();
        while (true) {
            for (Task t : tasksList) {
                if (t.isActive()) {
                    long currentMinute = getTimeInMinutes(currentDate);
                    Date next = t.nextTimeAfter(currentDate);
                    long taskMinute = getTimeInMinutes(next);
                    if ((t.isRepeated() && t.getEndTime().after(currentDate) || !t.isRepeated()) && currentMinute == taskMinute) {
                        showNotification(t);
                    }
                }
            }
            try {
                Thread.sleep((long) MILLISECONDS_IN_SEC * SECONDS_IN_MIN);
            } catch (InterruptedException e) {
                log.error("thread interrupted exception");
            }
            currentDate = new Date();
        }
    }
    public static void showNotification(Task task){
        log.info("push notification showing");
        Platform.runLater(() -> Notifications.create().title("Task reminder").text("It's time for " + task.getDescription()).showInformation());
    }
    private static long getTimeInMinutes(Date date){
        return date.getTime()/ MILLISECONDS_IN_SEC / SECONDS_IN_MIN;
    }
}