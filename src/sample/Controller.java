package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXToggleNode;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class Controller {

    @FXML
    private JFXButton playButton;

    @FXML
    private JFXButton nextButton;

    @FXML
    private JFXButton prevButton;

    @FXML
    private Label curTimeLabel;

    @FXML
    private JFXSlider musicSlider;

    @FXML
    private JFXSlider volumeSlider;

    @FXML
    private TableView<Music> musicsTable;

    @FXML
    private JFXToggleNode select;

    @FXML
    private JFXToggleNode exit;

    @FXML
    private JFXToggleNode deleteItem;

    @FXML
    private TableColumn<Music, Integer> numberColumn;

    @FXML
    private TableColumn<Music, String> nameColumn;

    @FXML
    private TableColumn<Music, String> pathColumn;
    FileChooser fileChooser;
    MediaPlayer mediaPlayer = null;
    int indexCurTrack = -1;
    String curTime = "";

    @FXML
    void initialize() {

        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        numberColumn.setStyle("-fx-alignment: CENTER;");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("refactorPath"));
        pathColumn.prefWidthProperty().bind(
                musicsTable.widthProperty()
                        .subtract(numberColumn.widthProperty())
                        .subtract(nameColumn.widthProperty())
                        .subtract(2)
        );

        fileChooser = new FileChooser();

        exit.setOnAction(actionEvent -> System.exit(0));


        musicsTable.setOnKeyPressed(keyEvent -> {
            final Music selectedItem = musicsTable.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                    //Delete or whatever you like:
                    musicsTable.getItems().remove(selectedItem);
                }
            }
        });
        deleteItem.setOnAction(actionEvent -> {
            musicsTable.getItems().clear();
            mediaPlayer = null;
            indexCurTrack = -1;
        });


        select.setOnAction(actionEvent -> {
            List<File> files = fileChooser.showOpenMultipleDialog(select.getParent().getScene().getWindow());
            if (files != null)
                for (var file : files) {
                    if (file != null) {
                        musicsTable.getItems().addAll(new Music(musicsTable.getItems().size() + 1, file.getName(), file.toURI().toString()));
                    }
                }
        });

        musicSlider.valueProperty().addListener(musicTimeListener);
        volumeSlider.valueProperty().addListener(volumeListener);

        TableView.TableViewSelectionModel<Music> selectionModel = musicsTable.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(tablelistener);

        playButton.setOnAction(actionEvent -> {
            if (mediaPlayer != null) {
                var status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PAUSED) {
                    mediaPlayer.play();
                } else {
                    mediaPlayer.pause();
                }
            }
        });

        nextButton.setOnAction(actionEvent -> {
            if (indexCurTrack == -1)
                return;
            indexCurTrack++;
            if (indexCurTrack == musicsTable.getItems().size())
                indexCurTrack = 0;
            Play(musicsTable.getItems().get(indexCurTrack).getPath());
        });

        prevButton.setOnAction(actionEvent -> {
            if (indexCurTrack == -1)
                return;
            indexCurTrack--;
            if (indexCurTrack < 0)
                indexCurTrack = musicsTable.getItems().size() - 1;
            Play(musicsTable.getItems().get(indexCurTrack).getPath());
        });
    }


    void SetTime() {
        var sec = ((int) mediaPlayer.getCurrentTime().toSeconds()) % 60;
        if (sec < 10) {
            curTime = (int) mediaPlayer.getCurrentTime().toMinutes() + ":0" + sec;
        } else {
            curTime = (int) mediaPlayer.getCurrentTime().toMinutes() + ":" + sec;
        }
        curTimeLabel.setText(curTime);
    }


    class StyleRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {
        @Override
        public TableRow<T> call(TableView<T> tableView) {
            return new TableRow<>() {
                @Override
                protected void updateItem(T paramT, boolean b) {
                    if (getIndex() == indexCurTrack) {
                        setStyle("-fx-background-color: #04B404;-fx-text-background-color: white;");

                    } else {
                        setStyle(null);
                    }
                    super.updateItem(paramT, b);
                }
            };
        }

    }

    ChangeListener<Duration> curTimeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
            musicSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
            SetTime();
        }
    };

    void Play(String path) {
        if (mediaPlayer != null)
            mediaPlayer.stop();
        mediaPlayer = new MediaPlayer(new Media(path));
        mediaPlayer.play();
        mediaPlayer.setOnReady(() -> {
            musicSlider.setMin(0);
            musicSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
            musicSlider.setValue(0);
        });
        mediaPlayer.currentTimeProperty().addListener(curTimeListener);
        musicsTable.setRowFactory(new StyleRowFactory<>());
        musicsTable.refresh();
    }

    ChangeListener<Music> tablelistener = (val, oldVal, newVal) -> {
        if (newVal != null) {
            Play(newVal.getPath());
            indexCurTrack = newVal.number - 1;
        }
    };

    InvalidationListener musicTimeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (musicSlider.isPressed() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(musicSlider.getValue()));
            }
        }
    };

    InvalidationListener volumeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (mediaPlayer != null)
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        }
    };
}
