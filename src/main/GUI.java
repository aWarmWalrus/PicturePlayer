package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class GUI extends AnchorPane {
	
	final Stage main;
	final Group root;
	final String address;
	static int SUBIMAGE_WIDTH = 50;
	static int SUBIMAGE_HEIGHT = 50;
	public File tempDir;
	BufferedImage img;
	
	//FXML IDs
	@FXML Button playButton;
	@FXML ImageView theImage;
	
	public GUI(Stage mainStage) {
		main = mainStage;
		root = (Group) main.getScene().getRoot();
		//Load the FXML
		FXMLLoader fxmlLoader = 
				new FXMLLoader(getClass().getResource("gui.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        address = System.getProperty("user.dir");
//		System.out.println(new File(address + "\\tempImages").mkdir());
//		System.out.println("new dir" + newDirectory.getAbsolutePath());
//		this.setWidth(main.getWidth());
//		this.setHeight(main.getHeight());
	}
	
	@FXML protected void loadImage() {
		
		//Get the image file from the user.
		FileChooser c = new FileChooser();
		File imageFile = c.showOpenDialog(main);
		String filePath = imageFile.getAbsolutePath();
		Image pic = new Image("file:///" + filePath);
		try {
			img = ImageIO.read(imageFile);
		} catch (IOException e) {
			System.out.println("what");
		}
		
		//create the new directory
		tempDir = new File(address + "\\tmpimg");
		if(tempDir.mkdir()){
			System.out.println("* Temporary directory successfully created:"
					+ "\n   " + tempDir.getAbsolutePath());
		} else {
			System.out.println("! Error: directory already initialized. Attempting clean...");
			if(clean()){
				System.out.println("* Clean successful.");
				if(tempDir.mkdir()) {
					System.out.println("* Temporary directory successfully created:"
							+ "\n   " + tempDir.getAbsolutePath());
				} else {
					System.out.println("! Error: attempt to reinitialize failed.");
					return;
				}
			} else {
				System.out.println("! Error: attempt to reclean failed.");
				return;
			}
		}
		
		//each block is 100x100 pixels
		int columns = (int) pic.getWidth() / SUBIMAGE_WIDTH;
		int rows = (int) pic.getHeight() / SUBIMAGE_HEIGHT;
		for(int col = 0; col < columns; col++) {
			for(int row = 0; row < rows; row++) {
				File newImage = new File(address + "\\tmpimg\\" + row+","+col+ ".jpg");
				try {
					ImageIO.write(img.getSubimage(col * SUBIMAGE_WIDTH,
												  row * SUBIMAGE_HEIGHT, 
												  SUBIMAGE_WIDTH, 
												  SUBIMAGE_HEIGHT), 
								  "jpg", 
								  newImage);
				} catch (IOException e) {
					System.out.println("Error while writing to a file");
					return;
				}
				Image sub = new Image("file:///" + newImage.getAbsolutePath());
				final ImageView subIV = new ImageView(sub);
				subIV.setX(col*50);
				subIV.setY(row*50);
				subIV.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent arg0) {
						subIV.setScaleX(3);
						subIV.setScaleY(3);
//						subIV.setMouseTransparent(true);
					}
				});
				subIV.setOnMouseExited(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent arg0) {
						subIV.setScaleX(1);
						subIV.setScaleY(1);
//						subIV.setMouseTransparent(false);
					}
				});
				root.getChildren().add(subIV);
			}
		}
//		clean();
//		theImage.setImage(img.getSubimage(10, 10, 100, 100));
//		theImage.setImage(pic);
//		theImage.autosize();
		theImage.setFitHeight(pic.getHeight());
		theImage.setFitWidth(pic.getWidth());

		main.setHeight(theImage.getFitHeight());
		main.setWidth(theImage.getFitWidth());
	}
	
	private boolean clean() {
		for(File f : tempDir.listFiles()){
			if(!f.delete()) {
				System.out.println("! Error deleting file: " + f.getName());
				return false;
			}
		}
		if(tempDir.delete()) {
			System.out.println("*Temporary directory successfully deleted");
		} else {
			System.out.println("bad");
			return false;
		}
		return true;
	}
}
