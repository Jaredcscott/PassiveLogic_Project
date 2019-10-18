/**
 *PassiveLogic Project
 *Jared Scott
 *Heat Flow Simulator 
 */
 
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Setting up the stage and panes with icons and a back ground image
        primaryStage.getIcons().add(new Image("icon.png"));
        ImageView background = new ImageView("background.png");
        primaryStage.setTitle("Heat Transfer Simulator");
        BorderPane borderPane = new BorderPane(); //Main pane
        primaryStage.setScene(new Scene(borderPane, 570, 620));
        primaryStage.setResizable(false); //Making the window size static
        Label title = new Label("\t\t  Welcome To Heat Transfer Simulator\n Please Enter The Values Indicated Below And Press 'Simulate'");
        title.setFont(new Font("Arial", 20));
        VBox inputPane = new VBox(); //This will hold the input boxes for formatting purposes

        //Creating boxes for input structure
        VBox inputBox = new VBox();
        VBox inputBox2 = new VBox();
        HBox inputAll = new HBox();

        //Creating the input fields and their corresponding labels
        Label panelSizeLab = new Label("Size Of Panel:");
        panelSizeLab.setFont(new Font("Arial", 21));
        TextField panelSize = new TextField();
        panelSize.setPromptText("m^2");
        
        Label waterVolSystemLab = new Label("Volume Of Water In System:");
        waterVolSystemLab.setFont(new Font("Arial", 21));
        TextField waterVolSystem = new TextField();
        waterVolSystem.setPromptText("Liters");
        
        Label tempOfSystemLab = new Label("Temperature Of System At Start:");
        tempOfSystemLab.setFont(new Font("Arial", 21));
        TextField tempOfSystem = new TextField();
        tempOfSystem.setPromptText("Degrees Celsius");

        Label timeLab = new Label("Simulation Time:");
        timeLab.setFont(new Font("Arial", 21));
        TextField time = new TextField();
        time.setPromptText("Minutes");

        Button simulate = new Button();
        simulate.setText("Simulate");
        simulate.setFont(new Font("Arial", 20));
        

        //Creating the event handler for running the simulation.
        simulate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                /**
                 * This contains the logic for simulating the heat transfer
                 * Assumptions
                 * Cloud cover is neglected
                 * Rate of energy coming form the sun is considered constant
                 * The fluid in the pipes is water.
                 * It is assumed that the pump causes the temperature of the system to stay homogeneous
                 * Specific heat of water: 4.186 joule/gram per degree C
                 * (Source: http://hyperphysics.phy-astr.gsu.edu/hbase/Tables/sphtt.html#c1)
                 * Weight of water 1 kg per liter
                 * Average energy absorbed by solar panels that becomes heat: 47%
                 * Source: http://solarcellcentral.com/limits_page.html)
                 * Average max temperature that an object can achieve in sunlight: 66 C
                 * (Source: https://physics.stackexchange.com/questions/186910/how-hot-can-metal-get-in-sunlight)
                 * Amount of energy coming from the sun: 1360 W (1360 (joules)/ second) per square meter  
                 * (Source: https://earthobservatory.nasa.gov/features/EnergyBalance/page2.php)
                 */
                boolean inputValid = false;
                boolean maxTempReached = false;
                float maxTempTime = 0;
                float tempSystemS = 0;
                float tempSystemE = 0;
                float simTime = 0;
                float energyTransfered = 0;
                float panelSizeF = 0;
                float massWaterInSystem = 0;
                float tempDifSystem = 0;
                //Validating input                
                try { 
                    panelSizeF = Float.valueOf(panelSize.getText());
                    tempSystemS = Float.valueOf(tempOfSystem.getText());
                    massWaterInSystem = Float.valueOf(waterVolSystem.getText()) * 1000; //Converting to grams
                    simTime = Float.valueOf(time.getText()) * 60; //Converting to seconds
                    inputValid  = true;
                }
                catch(Exception e) {
                    inputValid = false;
                }
                
                if (inputValid) {
                    /**
                     * I wanted to keep track of many more factors for accuracy. 
                     * Spent quite a bit of time trying to account for mass flow rates, radiant heat loss, enthalpy etc.
                     * This became very complicated very quickly so I stuck with an unrealistic simple model.
                     * Used the equation Q = mc(dT) 
                     * Q = energy transfer
                     * m = mass
                     * c = specific heat
                     * dT = Change of temperature  
                     * Simple model of heat increase of the system
                     */
                     for (int i = 0; i < simTime; i++){
                        //Amount of energy absorbed by the panel from the sun in the given time
                        energyTransfered += .47f * 1360 * panelSizeF; //.47 comes from average efficiency of panels see assumptions
                        tempDifSystem = (energyTransfered / (massWaterInSystem * 4.186f)); //4.186 being the specific heat of water see assumptions
                        tempSystemE = tempSystemS + tempDifSystem;
                    
                        if (tempSystemE > 66 && !maxTempReached) {
                            maxTempReached = true;
                            maxTempTime = i/60;
                        }
                    }
                }
                else {
                    System.out.println("Invalid Inputs");
                }

                //Setting up a secondary stage to display the results of the simulation
                final Stage dialog = new Stage();
                dialog.setTitle("Results");
                dialog.getIcons().add(new Image("icon.png"));
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(primaryStage);
                VBox dialogVbox = new VBox(20);
                if (inputValid) {
                    if (maxTempReached) {
                        tempSystemE = 66; //supposed maximum temperature that an object can get in sunlight
                        Text maxTemp = new Text("Maximum Temperature Achieved\nExcess Heat Lost To Environment\nTime Taken To Reach Maximum Temperature (Minutes): " + maxTempTime);
                        dialogVbox.getChildren().add(maxTemp);
                    }
                    Text resultSolarTransfer = new Text("The Energy Transferred To The Solar Panel (Joules): " + energyTransfered);
                    Text resultTempDif = new Text("Total Heat Added To The System (Degrees Celsius): " + tempDifSystem);
                    Text resultSystemTemp = new Text("The Final Temperature Of The System (Degrees Celsius): " + tempSystemE);
                    dialogVbox.getChildren().addAll(resultSolarTransfer, resultTempDif, resultSystemTemp);
                }
                else {
                    Text inputIsInvalid = new Text("Input Is Invalid\nAll Input Must Be Numeric");
                    dialogVbox.getChildren().add(inputIsInvalid);
                }
                Scene dialogScene = new Scene(dialogVbox, 400, 200);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });

        //Adding the nodes to their respective boxes, adding the boxes to the panes
        inputBox.getChildren().addAll(panelSizeLab, waterVolSystemLab, tempOfSystemLab, timeLab);
        inputBox2.getChildren().addAll(panelSize, waterVolSystem, tempOfSystem, time);
        inputAll.getChildren().addAll(inputBox, inputBox2);
        inputPane.getChildren().addAll(inputAll, simulate);
        borderPane.setTop(title);
        borderPane.setCenter(background);
        borderPane.setBottom(inputPane);
        simulate.requestFocus();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}