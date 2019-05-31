package src;

import javafx.beans.DefaultProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

@DefaultProperty("children")
public class BottomTable extends Region {
    private Pane pane;
    private int currentRaw = 1;
    private List<TextField> m_angleInputs = new ArrayList<>();
    private List<TextField> m_distanceInputs = new ArrayList<>();
    private List<CheckBox> kpBtns = new ArrayList<>();
    private List<CheckBox> scBtns = new ArrayList<>();
    private List<CheckBox> autoBtns = new ArrayList<>();

    private int currentOffsetY = 10;
    private int fieldW = 70;
    private int btnW = 50;
    private int rawHeight = 20;
    private CheckBox m_kpCheckBox = new CheckBox("КП");
    private CheckBox m_autoCheckBox = new CheckBox("АВТО");
    private CheckBox m_scCheckBox = new CheckBox("СЦ");


    private int startedOffset = 10;
    private int currentOffset = startedOffset;
    private int fieldWidth = fieldW + currentOffset;
    //private int btnWidth = btnW + currentOffset;
    private int btnWidth = 80;

    private Button m_delBtn;
    private ScrollPane scrollPane;
    private AddTarget addTargetFunc;
    private int height = 6 * (rawHeight + startedOffset);

    private Locator locator;


    // ******************** Constructors **************************************
    public BottomTable(Locator locator) {
        locator.setBottomTable(this);

        this.locator = locator;
        Rectangle rect1 = new Rectangle(500, height);
        this.addTargetFunc = locator::addNewTarget;
        scrollPane = new ScrollPane(pane);
        scrollPane.setMaxSize(500, 150);

        ///////////////////////////////////////////////////////////////////////////////////
        m_delBtn = new Button(" Удалить ");
        m_delBtn.setStyle("-fx-background-color: #ff0000; ");

        m_delBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
//            Button rect = new Button("m_delBtn");
//            rect.getStyleClass().add("m_delBtn");
//            rect.relocate(10 + 20  + 20, 20);
//            pane.getChildren().add(rect);
          //  getNewRaw()
            removeTarget();
        });

        m_delBtn.relocate(430, 10);
        ///////////////////////////////////////////////////////////////////////////////////

        pane = new Pane(scrollPane);
        pane.getChildren().addAll(rect1, m_delBtn);
//        pane.setMaxSize(500, 50);
        pane.setPrefSize(500, 50);
        getChildren().setAll(pane);
        getNewRaw();
    }

    private void removeTarget() {
        resetRow();
        locator.removeTarget();
    }

    public void setDisableOnScBtn(int index, boolean isDisable) {
        scBtns.get(index).setDisable(isDisable);
    }

    private void resetRow() {
        m_kpCheckBox.setDisable(false);

        m_kpCheckBox.setSelected(false);
        m_autoCheckBox.setSelected(false);
        m_scCheckBox.setSelected(false);

        m_angleInputs.get(0).setDisable(false);
        m_angleInputs.get(0).setText("");
        m_distanceInputs.get(0).setDisable(false);
        m_distanceInputs.get(0).setText("");
    }

    private void getNewRaw() {

        TextField textField1 = new TextField();
        textField1.setMaxSize(fieldW, rawHeight);
        TextField textField2 = new TextField();
        textField2.setMaxSize(fieldW, rawHeight);


        m_kpCheckBox.setPrefSize(50, rawHeight);
        m_autoCheckBox.setPrefSize(70, rawHeight);
        m_scCheckBox.setPrefSize(50, rawHeight);

        m_kpCheckBox.setStyle("-fx-background-color: #FFFFFF; ");
        m_autoCheckBox.setStyle("-fx-background-color: #FFFFFF; ");
        m_scCheckBox.setStyle("-fx-background-color: #FFFFFF; ");
        m_kpCheckBox.setDisable(false);
        m_autoCheckBox.setDisable(false);
        m_scCheckBox.setDisable(true);

        //btn4.setDisable(true);

        textField1.relocate(currentOffset, currentOffsetY);
        textField2.relocate(currentOffset += fieldWidth, currentOffsetY);
        m_kpCheckBox.relocate(currentOffset += btnWidth, currentOffsetY);
        m_autoCheckBox.relocate(currentOffset += 60, currentOffsetY);
        m_scCheckBox.relocate(currentOffset += btnWidth, currentOffsetY);
        currentOffset = startedOffset;

        m_angleInputs.add(textField1);
        m_distanceInputs.add(textField2);
        kpBtns.add(m_kpCheckBox);
        autoBtns.add(m_autoCheckBox);
        scBtns.add(m_scCheckBox);

/*
        btn1.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            int i = kpBtns.indexOf(btn1);
            int angle = Integer.valueOf(m_angleInputs.get(i).getText());
            double distance = Double.valueOf(m_distanceInputs.get(i).getText());
//            System.out.println(angle);
//            System.out.println(distance);
            if(distance>0 && distance<100 && angle >= 0 && angle < 360) {
                addTargetFunc.apply(angle, distance);
            }

            kpBtns.get(i).setDisable(true);
            m_angleInputs.get(i).setDisable(true);
            m_distanceInputs.get(i).setDisable(true);

        });
*/

        m_kpCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                int i = 0;
                if (!new_val) { return; }
                int angle = Integer.valueOf(m_angleInputs.get(i).getText());
                angle = (360 + angle) % 360;
                double distance = Double.valueOf(m_distanceInputs.get(i).getText());
                if(distance>0 && distance<80) {
                    addTargetFunc.apply(angle, distance, () -> removeTarget());
                    kpBtns.get(i).setDisable(true);
                    m_angleInputs.get(i).setDisable(true);
                    m_distanceInputs.get(i).setDisable(true);
                }
            }
        });

/*
        btn2.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            int i = autoBtns.indexOf(btn2);
            int angle = Integer.valueOf(m_angleInputs.get(i).getText());
            double distance = Double.valueOf(m_distanceInputs.get(i).getText());

            locator.onAutoClicked();
        });
*/

        m_autoCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                m_autoCheckBox.setSelected(newVal);
                locator.onAutoClicked(newVal);
            }
        });

        m_scCheckBox.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            locator.onScClicked();
        });

//        scrollPane.setVvalue(1.0);

        pane.getChildren().addAll(textField1, textField2, m_kpCheckBox, m_autoCheckBox, m_scCheckBox);
        currentOffsetY += startedOffset + rawHeight;

        currentRaw++;

        if (currentRaw == 6) {
            m_delBtn.setVisible(false);
        }
    }

}

