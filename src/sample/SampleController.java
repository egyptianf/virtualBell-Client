package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SampleController {


    public Label connected;

    public void initialize(){
        connected.textProperty().bind(ChatClientEndpoint.statusProperty);

    }


}
