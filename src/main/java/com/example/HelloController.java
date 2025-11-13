package com.example;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField messageField;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        messageView.setItems(model.getMessages());

        messageView.setCellFactory(list -> new ListCell<>() {
            private final Label messageLabel = new Label();
            private final HBox bubble = new HBox(messageLabel);
            {
                bubble.setPadding(new Insets(5, 10, 5, 10));
                bubble.setMaxWidth(200);
                messageLabel.setWrapText(true);
                bubble.getStyleClass().add("chat-bubble");
            }

            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // Format tid + text
                    java.time.LocalTime time = java.time.Instant.ofEpochSecond(item.time())
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalTime();
                    String formattedTime = time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

                    messageLabel.setText(formattedTime + "\n" + item.message());
                    setGraphic(bubble);
                }
            }
        });

        model.messageToSendProperty().bind(messageField.textProperty());

    }

    public void sendMessage(ActionEvent actionEvent) {
        String message = messageField.getText();
        if(message == null || message.isBlank()){
            showTemporaryAlert("You must write something before sending!");
            return;
        }

        model.sendMessage();
        messageField.clear();
    }

    private void showTemporaryAlert(String alertMessage) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(alertMessage);
        alert.initOwner(messageField.getScene().getWindow());

        alert.show();

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
        }).start();
    }
}
