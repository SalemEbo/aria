package ariafx.gui.fxml.control;

import java.net.URL;
import java.util.ResourceBundle;

import ariafx.core.download.Chunk;
import ariafx.core.download.Link;
import ariafx.core.url.type.DownState;
import ariafx.gui.fxml.imp.MovingStage;
import ariafx.gui.fxml.imp.ProgressCircle;
import ariafx.gui.fxml.imp.ProgressStyled;
import ariafx.gui.fxml.imp.ProgressStyledTableCell;
import ariafx.gui.manager.ItemBinding;
import ariafx.tray.TrayUtile;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DownUi implements Initializable {

	Stage stage;
	Link link;
	int id;
	
	public static URL Fxml;
	
	public DownUi() {
		this.stage = new Stage(StageStyle.UNDECORATED);
		if(Fxml == null)
			Fxml = getClass().getResource("DownUi.xml");
	}
	
	public DownUi(int id , Link link) {
		this();
		this.id  = id;
		this.link = link;
		this.stage.setTitle(link.getFilename());
	}

	public Stage getStage() {
		return stage;
	}
	
	public void setScene(Scene scene) {
		stage.setScene(scene);
	}
	
	public void setScene(AnchorPane anchorPane) {
		setScene(new Scene(anchorPane));
	}
	
	public void showAndMove(AnchorPane anchorPane) {
		setScene(anchorPane);
		stage.show();
		MovingStage.pikeToMoving(stage, anchor);
	}
	
	public void show() {
//		TrayUtile.removeFromTray(this);
		stage.show();
		stage.setIconified(false);
//		MovingStage.pikeToMoving(stage, anchor);
	}
	
	/*public void setStage(Stage stage) {
		this.stage = stage;
	}*/

	public Link getLink() {
		return link;
	}
	public String getFilename() {
		return link.getFilename();
	}

	public void setLink(Link link) {
		this.link = link;
	}
	
	boolean process_speed = true;  // true_false;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		progress.setDoneText("Download");
		
		link.runningProperty().addListener((obv, old, value)->{
			if(!link.isInitState()){
				if(value || link.getState() == State.CANCELLED ){
					show();
				}else{
					cancel(null);
				}
				
			}
//			else if( link.canStartCopy()){
//				if(value || link.getState() == State.CANCELLED ){
//					show();
//				}else{
//					cancel(null);
//				}
//			}
			
		});
		
		link.downStateProperty().addListener((obv, old, value)->{
			if(!link.isInitState()){
				if (value == DownState.Downloading){
					updateProcessTable();
					show();
					TrayUtile.addTListTray(this);
					
				}else if (value == DownState.Complete){
					TrayUtile.removeFromListTray(this);
				}else{
					TrayUtile.removeFromListTray(this);
				}
			}
			
		});
		
		subprocess.setOnAction((e)->{
			addonsBox.setLayoutX(0);
			link.stopCollectSpeedData();
			stage.setHeight(450);
			process_speed = true;
		});
		
		speed.setOnAction((e)->{
			addonsBox.setLayoutX(-540);
			link.canCollectSpeedData();
			stage.setHeight(450);
			process_speed = false;
		});
		
		showInfo.setOnAction((e)->{
			if(showInfo.getText().equals("<<")){	// hide
				stage.setHeight(240);
				showInfo.setText(">>");
				clearProcessTable();
				link.stopCollectSpeedData();
			} else {								// show
				stage.setHeight(450);
				showInfo.setText("<<");
				if(process_speed){
					updateProcessTable();
				} else {
					link.canCollectSpeedData();
				}
			}
			
		});
		
		toggleState.setOnAction((e)->{
			displayBox.setLayoutX(0);
		});
		
		toggleLimite.setOnAction((e)->{
			displayBox.setLayoutX(-540);
		});
		
		toggleFinish.setOnAction((e)->{
			displayBox.setLayoutX(-1080);
		});
		
		
		
		initTable();
		//chunksTable.getItems().addAll(link.getChunks());
		
		ItemBinding.bindItem(link, this);
		MovingStage.pikeToMoving(stage, anchor);
		
		labelCurrentTime.textProperty().bind(link.currentTimeProperty());

//    	TrayUtile.addTListTray(this);
	}
	
	void initTable(){
		chunkDone.setCellValueFactory(new PropertyValueFactory<Chunk, String>( "done"));
		chunkSize.setCellValueFactory(new PropertyValueFactory<Chunk, String>( "size"));
		chunkStateCode.setCellValueFactory(new PropertyValueFactory<Chunk, String>( "stateCode"));
		chunkID.setCellValueFactory(new PropertyValueFactory<Chunk, Integer>( "id"));
		chunkProgress.setCellValueFactory(new PropertyValueFactory<Chunk, Double>( "progress"));
		
		chunkProgress.setCellFactory( ProgressStyledTableCell.<Chunk> forTableColumn());
		
		
		
		chunksTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
	}
	
	private void clearProcessTable(){
		chunksTable.getItems().clear();
		subprogress.getChildren().clear();
	}
	
	private void updateProcessTable(){
		clearProcessTable();
//		if(!process_speed) return;
		Chunk[] chunks = link.getChunks();
		if(chunks == null){
			try {
				Thread.sleep(500);
				chunks = link.getChunks();
			} catch (Exception e) {
			}
		}
		if(!(chunks == null) ){
			chunksTable.getItems().addAll(chunks);
			for (Chunk chunk : chunks) {
				ProgressBar progress = ProgressStyled.CreateProgressFlat();
				progress.progressProperty().bind(chunk.progressProperty());
				progress.setPrefHeight(subprogress.getPrefHeight());
				
//				progress.setPrefWidth(subprogress.getPrefWidth()/chunks.length);
//				progress.setPrefWidth( (double)( (double)(chunk.range[1]-chunk.range[0]) / (double)link.getLength()) *(double)subprogress.getWidth() );

//				progress.setMaxWidth ( (double)( (double)(chunk.range[1]-chunk.range[0]) / (double)link.getLength()) *(double)subprogress.getWidth() );
//				progress.setMinWidth ( (double)( (double)(chunk.range[1]-chunk.range[0]) / (double)link.getLength()) *(double)subprogress.getWidth() );
				
				SimpleDoubleProperty testDoneSize = new SimpleDoubleProperty(1);
				
//				chunk.progressProperty().multiply(chunk.range[1]-chunk.range[0]).divide(link.getLength()).multiply(subprocess.getWidth());
				testDoneSize.bind(chunk.progressProperty().multiply(chunk.getRange()[1]-chunk.getRange()[0]).divide(link.getLength()).multiply(subprocess.getWidth()));
				
				progress.minWidthProperty().bind( testDoneSize);
//				progress.maxWidthProperty().multiply(chunk.progressProperty().multiply(chunk.range[1]-chunk.range[0]).divide(link.getLength()).multiply(subprocess.getWidth()));
				
				
				subprogress.getChildren().add(progress);
			}
		}
	}
	
	
//	public void initFinish() {
//		textSaveTo.setText(link.getSaveto());
//	}

	/**-----------------------------@FXML--------------------------**/
	@FXML
	public AnchorPane anchor;

    @FXML
    public TextField textMaxLimit;
    
    @FXML
    private HBox addonsBox, displayBox;
    
    @FXML
    private TableView<Chunk> chunksTable;
    
    @FXML
    private TableColumn<Chunk, String> chunkStateCode;

    @FXML
    private TableColumn<Chunk, Double> chunkProgress;
    
    @FXML
    private TableColumn<Chunk, String> chunkSize;
    
    @FXML
    private TableColumn<Chunk, String> chunkDone;

    @FXML
    private TableColumn<Chunk, Integer> chunkID;
    
    @FXML
    private ToggleButton subprocess,  speed, showInfo;
    
    @FXML
	public Text remining;
    
    @FXML
    private HBox subprogress;
    
    @FXML
    private ToggleButton toggleState, toggleLimite, toggleFinish;
    
    @FXML
    public Label panename, labelTransferRate, 
    	labelAddress, labelTransferRate2, 
    	labelStatus, labelDownloaded, labelFileSize, 
    	labelResume, filename, labelTimeLeft, labelCurrentTime;
    
    @FXML
    public Text textSaveTo;
    
    @FXML
    public CheckBox checkUseSpeedLimit, 
    		showCompleteDialoge, checkExitProgram, 
    		checkHangUpModem, checkTurnOff,
    		checkRememberLimit;


    @FXML
    public ProgressCircle progress;

    @FXML
    public Button selector;


    @FXML
    public  LineChart<Integer, Double> chart;
    @FXML
    public NumberAxis yAxis, xAxis;

    @FXML
    void useSpeedLimter(ActionEvent event) {

    }

    @FXML
    void rememberSpeedLimter(ActionEvent event) {

    }

    @FXML
    void showDownloadCompleteDialoge(ActionEvent event) {

    }

    @FXML
    void hangUpModem(ActionEvent event) {

    }

    @FXML
    void CloseProgrameAfterDone(ActionEvent event) {

    }

    @FXML
    void shutDownOS(ActionEvent event) {
    	
    }

    @FXML
    void minimize(ActionEvent event) {
    	stage.setIconified(true);
    }
    
    @FXML
    void hideToTray(ActionEvent event) {
    	stage.hide();
    	TrayUtile.addTListTray(this);
    }

    @FXML
    void cancel(ActionEvent event) {
    	if(link.isRunning()){
    		if(link.getDownState() != DownState.Complete)
    			link.cancel();
    	}
    	link.stopCollectSpeedData();
    	stage.close();
    	
    	TrayUtile.removeFromListTray(this);
    }

    @FXML
    void checkLink(ActionEvent event) {
    	Button button = (Button) event.getSource();
    	if(link.isRunning()){
    		link.cancel();
    		button.setText("Resume");
    	}else{
    		link.restart();
    		button.setText("Pause");
    	}
    }

	
    
    
    

}

