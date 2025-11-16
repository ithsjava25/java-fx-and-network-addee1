package com.example;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.net.URI;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> messageView;
    private boolean lastActionWasFile =  false;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField messageField;

    @FXML
    private void attachFile() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.txt")
        );

        File file = chooser.showOpenDialog(messageField.getScene().getWindow());
        if (file != null) {
            lastActionWasFile = true;
            model.sendFile(file);
        }
    }

    @FXML
    private void initialize() {

        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }

        messageView.setItems(model.getMessages());

        // quickfix :D prevent listview cell selection which forces a re-render and
        // causes image messages to flicker or disappear when clicking or scrolling.
        messageView.setSelectionModel(new NoSelectionModel<>());

        messageView.setCellFactory(list -> new ListCell<>() {

            private final Label textLabel = new Label();
            private final HBox textBubble = new HBox(textLabel);

            {
                textBubble.setPadding(new Insets(5, 10, 5, 10));
                textBubble.setMaxWidth(200);
                textLabel.setWrapText(true);
                textBubble.getStyleClass().add("chat-bubble");
            }

            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                if (item.attachment() != null &&
                        item.attachment().type() != null &&
                        item.attachment().type().startsWith("image") &&
                        item.attachment().url() != null) {

                    try {
                        Image image = new Image(item.attachment().url(), 200, 0, true, true);
                        ImageView imageView = new ImageView(image);
                        imageView.setPreserveRatio(true);

                        HBox imageBubble = new HBox(imageView);
                        imageBubble.setPadding(new Insets(5, 10, 5, 10));
                        imageBubble.setMaxWidth(200);
                        imageBubble.getStyleClass().add("chat-bubble");

                        setText(null);
                        setGraphic(imageBubble);
                    } catch (Exception e) {
                        Label err = new Label("[Image failed to load]");
                        HBox bubble = new HBox(err);
                        bubble.setPadding(new Insets(5, 10, 5, 10));
                        bubble.getStyleClass().add("chat-bubble");
                        setGraphic(bubble);
                    }
                    return;
                }

                if (item.attachment() != null && item.attachment().url() != null) {

                    String fileName = item.attachment().name();
                    String fileUrl = item.attachment().url();

                    Label icon = new Label("📄");
                    icon.setStyle("-fx-font-size: 20px;");

                    Label fileLabel = new Label(fileName);
                    fileLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

                    Button openBtn = new Button("Open file");
                    openBtn.setOnAction(e -> {
                        System.out.println("Opening: " + fileUrl);
                        new Thread(() -> {
                            try {
                                URI uri = new URI(fileUrl);
                                Desktop.getDesktop().browse(uri);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();
                    });


                    HBox fileBox = new HBox(10, icon, fileLabel, openBtn);
                    fileBox.setPadding(new Insets(5, 10, 5, 10));
                    fileBox.setMaxWidth(200);
                    fileBox.getStyleClass().add("chat-bubble");

                    setText(null);
                    setGraphic(fileBox);
                    return;
                }


                java.time.LocalTime time = java.time.Instant.ofEpochSecond(item.time())
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalTime();
                String formattedTime = time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

                Label msgLabel = new Label(formattedTime + "\n" + item.message());
                msgLabel.setWrapText(true);
                msgLabel.setMaxWidth(250);

                HBox msgBubble = new HBox(msgLabel);
                msgBubble.setPadding(new Insets(5, 10, 5, 10));
                msgBubble.setMaxWidth(300);
                msgBubble.getStyleClass().add("chat-bubble");

                setText(null);
                setGraphic(msgBubble);
            }

        });

        model.messageToSendProperty().bind(messageField.textProperty());
    }


    public void sendMessage(ActionEvent actionEvent) {
        String message = messageField.getText();
        if(!lastActionWasFile && (message == null || message.isBlank())){
            showTemporaryAlert("You must write something before sending!");
            return;
        }

        if (message != null && !message.isBlank()) {
            model.sendMessage();
        }

        messageField.clear();
        lastActionWasFile = false;
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

    private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {

        /*
            Quickfix to prevent the ListView from selecting cells when the user clicks on them.
            Otherwise it triggers full re render of the listcells when which causes message bubbles
            or images to flicker or dissapears on click or scroll
         */

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.emptyObservableList();
        }

        @Override public void selectIndices(int index, int... indices) { }
        @Override public void selectAll() { }
        @Override public void clearAndSelect(int index) { }
        @Override public void select(int index) { }
        @Override public void select(T obj) { }
        @Override public void clearSelection(int index) { }
        @Override public void clearSelection() { }
        @Override public boolean isSelected(int index) { return false; }
        @Override public boolean isEmpty() { return true; }
        @Override public void selectPrevious() { }
        @Override public void selectNext() { }
        @Override public void selectFirst() { }
        @Override public void selectLast() { }
    }
}

