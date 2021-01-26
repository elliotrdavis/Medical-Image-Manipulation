package application;
// Elliot Davis - 959547. This my own work
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

public class Main extends Application {
	short cthead[][][];
	short min, max;
	int topSlice, frontSlice, sideSlice, tSlice, fSlice;
	int actual_width = 256;
	int actual_height = 256;
	int actual_depth = 113;
	int max_width = (int) (actual_width * 1.5);
	int max_height = (int) (actual_height * 1.5);
	int max_depth = (int) (actual_depth * 1.5);
	Color colour = Color.LIGHTGREY;

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		ReadData();
		stage.setTitle("CT Head Viewer");

		Button mip_button = new Button("MIP");
		Button thumbnail_button = new Button("Thumbnail");
		Slider topSlider = new Slider(0, 112, 0);
		Slider frontSlider = new Slider(0, 255, 0);
		Slider sideSlider = new Slider(0, 255, 0);
		Slider resize = new Slider(10, max_width, 255);
		TextField rangeInputMinText = new TextField("1");
		TextField rangeInputMaxText = new TextField("113");
		TextField thumbnailSizeText = new TextField("50");
		Label topText = new Label("Top View: Slice Slider");
		Label frontText = new Label("Front View: Slice Slider");
		Label sideText = new Label("Side View: Slice Slider");
		Label resizeText = new Label("Resize");
		Label thumbnailText = new Label("Enter min and max range values in these two text boxes (int between (1-113)). "
				+ "  Enter size of thumbnail here (must be int between 0-384)");
		Label sliceClicked = new Label("");

		AnchorPane anchorPane = new AnchorPane();

		WritableImage medical_image_top = new WritableImage(max_width, max_height);
		ImageView imageViewTop = new ImageView(medical_image_top);

		WritableImage medical_image_front = new WritableImage(max_width, max_depth);
		ImageView imageViewFront = new ImageView(medical_image_front);

		WritableImage medical_image_side = new WritableImage(max_width, max_depth);
		ImageView imageViewSide = new ImageView(medical_image_side);

		mip_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MIP(medical_image_top, medical_image_front, medical_image_side);
			}
		});

		thumbnail_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double right, down, pixels = 0, startDown = 525;
				int rangeInputMin = Integer.parseInt(rangeInputMinText.getText()) - 1;
				int rangeInputMax = Integer.parseInt(rangeInputMaxText.getText());
				int thumbnailSize = Integer.parseInt(thumbnailSizeText.getText());
				tSlice = thumbnailSize;

				ImageView[] imageViewArray = new ImageView[113];

				WritableImage clear_thumbnail = new WritableImage(1600, 1600);
				ImageView clearThumbnailImageView = new ImageView(clear_thumbnail);
				clear(clear_thumbnail);
				AnchorPane.setTopAnchor(clearThumbnailImageView, 525.0);
				anchorPane.getChildren().addAll(clearThumbnailImageView);

				for (topSlice = rangeInputMin; topSlice < rangeInputMax; topSlice++) {
					WritableImage medical_image_thumbnail = new WritableImage(max_width, max_height);
					imageViewArray[topSlice] = new ImageView(medical_image_thumbnail);
					resize(medical_image_thumbnail);

					int counter = topSlice + 1;
					imageViewArray[topSlice].addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							sliceClicked.setText("Slice Clicked: " + counter);
						}
					});

					pixels = pixels + tSlice;
					if (pixels <= 1600) {
						down = startDown;
						right = pixels - tSlice;
					} else if (pixels <= 3200) {
						down = startDown + tSlice;
						right = pixels - 1700 + tSlice;
					} else if (pixels <= 4800) {
						down = startDown + tSlice * 2;
						right = pixels - 3250;
					} else {
						down = startDown + tSlice * 3;
						right = pixels - 4850;
					}

					AnchorPane.setTopAnchor(imageViewArray[topSlice], down);
					AnchorPane.setLeftAnchor(imageViewArray[topSlice], right);
					anchorPane.getChildren().addAll(imageViewArray[topSlice]);
				}
			}
		});

		topSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				topSlice = newValue.intValue();
				if (resize.getValue() == 255) {
					topSlider(medical_image_top);
				} else {
					resize(medical_image_top);
				}
			}
		});

		frontSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				frontSlice = newValue.intValue();
				frontSlider(medical_image_front);
			}
		});

		sideSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				sideSlice = newValue.intValue();
				sideSlider(medical_image_side);
			}
		});

		resize.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println(newValue.intValue());
				tSlice = newValue.intValue();
				clear(medical_image_top);
				resize(medical_image_top);
			}
		});

		// Labels
		AnchorPane.setLeftAnchor(topText, 40.0);
		AnchorPane.setLeftAnchor(frontText, 500.0);
		AnchorPane.setLeftAnchor(sideText, 800.0);
		AnchorPane.setLeftAnchor(resizeText, 280.0);
		AnchorPane.setTopAnchor(thumbnailText, 450.0);
		AnchorPane.setTopAnchor(sliceClicked, 475.0);
		AnchorPane.setLeftAnchor(sliceClicked, 750.0);

		// Sliders
		AnchorPane.setTopAnchor(topSlider, 30.0);
		AnchorPane.setLeftAnchor(topSlider, 100.0);
		AnchorPane.setTopAnchor(resize, 30.0);
		AnchorPane.setLeftAnchor(resize, 275.0);
		AnchorPane.setTopAnchor(frontSlider, 30.0);
		AnchorPane.setLeftAnchor(frontSlider, 500.0);
		AnchorPane.setTopAnchor(sideSlider, 30.0);
		AnchorPane.setLeftAnchor(sideSlider, 800.0);

		// Images
		AnchorPane.setTopAnchor(imageViewTop, 50.0);
		AnchorPane.setLeftAnchor(imageViewTop, 70.0);
		AnchorPane.setTopAnchor(imageViewFront, 50.0);
		AnchorPane.setLeftAnchor(imageViewFront, 475.0);
		AnchorPane.setTopAnchor(imageViewSide, 50.0);
		AnchorPane.setLeftAnchor(imageViewSide, 800.0);

		// Buttons
		AnchorPane.setTopAnchor(mip_button, 200.0);
		AnchorPane.setLeftAnchor(mip_button, 10.0);
		AnchorPane.setTopAnchor(thumbnail_button, 475.0);

		// Input Boxes
		AnchorPane.setTopAnchor(rangeInputMinText, 475.0);
		AnchorPane.setLeftAnchor(rangeInputMinText, 125.0);
		AnchorPane.setTopAnchor(rangeInputMaxText, 475.0);
		AnchorPane.setLeftAnchor(rangeInputMaxText, 325.0);
		AnchorPane.setTopAnchor(thumbnailSizeText, 475.0);
		AnchorPane.setLeftAnchor(thumbnailSizeText, 525.0);

		anchorPane.getChildren().addAll(imageViewTop, imageViewFront, imageViewSide, mip_button, topSlider, frontSlider,
				sideSlider, resize, topText, frontText, sideText, resizeText, thumbnail_button, rangeInputMinText,
				rangeInputMaxText, thumbnailSizeText, thumbnailText, sliceClicked);

		Scene scene = new Scene(anchorPane, 1600, 900);
		BackgroundFill background_fill = new BackgroundFill(colour, CornerRadii.EMPTY, Insets.EMPTY);
		Background background = new Background(background_fill);
		anchorPane.setBackground(background);
		stage.getIcons().add(new Image(getClass().getResourceAsStream("skull.png")));
		stage.setScene(scene);
		stage.show();
	}

	// Function to read in the cthead data set
	public void ReadData() throws IOException {
		// File name is hardcoded here - much nicer to have a dialog to select it and
		// capture the size from the user
		File file = new File("C:\\Users\\ellio\\OneDrive\\Desktop\\University\\Year 2\\CS-255\\CTHead\\src\\application\\CThead.raw");
		// Read the data quickly via a buffer (in C++ you can just do a single fread - I
		// couldn't find if there is an equivalent in Java)
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k; // loop through the 3D data set

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE; // set to extreme values
		short read; // value read in
		int b1, b2; // data is wrong Endian (check wikipedia) for Java so we need to swap the bytes
					// around

		cthead = new short[113][256][256]; // allocate the memory - note this is fixed for this data set
		// loop through the data reading it in
		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					// because the Endianess is wrong, it needs to be read byte at a time and
					// swapped
					b1 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					b2 = ((int) in.readByte()) & 0xff; // the 0xff is because Java does not have unsigned types
					read = (short) ((b2 << 8) | b1); // and swizzle the bytes around
					if (read < min)
						min = read; // update the minimum
					if (read > max)
						max = read; // update the maximum
					cthead[k][j][i] = read; // put the short into memory (in C++ you can replace all this code with one
											// fread)
				}
			}
		}
		System.out.println(min + " " + max); // diagnostic - for CThead this should be -1117, 2248
		// (i.e. there are 3366 levels of grey (we are trying to display on 256 levels
		// of grey)
		// therefore histogram equalization would be a good thing
	}

	public void topSlider(WritableImage image) {
		int i, j, c, w = actual_width, h = actual_height;
		PixelWriter image_writer = image.getPixelWriter();

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				short datum = cthead[topSlice][j][i];
				float col = (((float) datum - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++) {
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
		}
	}

	public void frontSlider(WritableImage image) {
		int i, c, k, w = actual_width, h = actual_depth;
		PixelWriter image_writer = image.getPixelWriter();

		for (k = 0; k < h; k++) {
			for (i = 0; i < w; i++) {
				short datum = cthead[k][frontSlice][i];
				float col = (((float) datum - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++) {
					image_writer.setColor(i, k, Color.color(col, col, col, 1.0));
				}
			}
		}
	}

	public void sideSlider(WritableImage image) {
		int j, c, k, w = actual_width, h = actual_depth;
		PixelWriter image_writer = image.getPixelWriter();

		for (k = 0; k < h; k++) {
			for (j = 0; j < w; j++) {
				short datum = cthead[k][j][sideSlice];
				float col = (((float) datum - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++) {
					image_writer.setColor(j, k, Color.color(col, col, col, 1.0));
				}
			}
		}
	}

	public void MIP(WritableImage image1, WritableImage image2, WritableImage image3) {
		int j, i, c, k, w = actual_width, h = actual_height, d = actual_depth;

		PixelWriter image_writer_top = image1.getPixelWriter();
		PixelWriter image_writer_front = image2.getPixelWriter();
		PixelWriter image_writer_side = image3.getPixelWriter();
		clear(image1);

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				int maximum_top = 1;
				for (k = 0; k < 112; k++) {
					maximum_top = Math.max(maximum_top, cthead[k][j][i]);
				}
				float colTop = (((float) maximum_top - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++) {
					image_writer_top.setColor(i, j, Color.color(colTop, colTop, colTop, 1.0));
				}
			}
		}

		for (j = 0; j < d; j++) {
			for (i = 0; i < w; i++) {
				int maximum_front = 1;
				int maximum_side = 1;
				for (k = 0; k < actual_depth; k++) { 
					maximum_front = Math.max(maximum_front, cthead[j][k][i]);
					maximum_side = Math.max(maximum_side, cthead[j][i][k]);
				}
				float colFront = (((float) maximum_front - (float) min) / ((float) (max - min)));
				float colSide = (((float) maximum_side - (float) min) / ((float) (max - min)));
				for (c = 0; c < 3; c++) {
					image_writer_front.setColor(i, j, Color.color(colFront, colFront, colFront, 1.0));
					image_writer_side.setColor(i, j, Color.color(colSide, colSide, colSide, 1.0));
				}
			}
		}
	}

	public void resize(WritableImage new_image) {
		int i, j, c, w = actual_width, h = actual_height;

		WritableImage old_image = new WritableImage(max_width, max_height);
		PixelReader pixel_reader = old_image.getPixelReader();
		PixelWriter new_image_writer = new_image.getPixelWriter();

		topSlider(old_image);

		for (j = 0; j < tSlice; j++) {
			for (i = 0; i < tSlice; i++) {
				double gRed, gGreen, gBlue;

				float y = (float) (j * (float) ((float) h / (float) tSlice));
				float x = (float) (i * (float) ((float) w / (float) tSlice));

				int bx = (int) Math.floor(x);
				int by = (int) Math.ceil(y);
				int fx = (int) x;
				int fy = (int) Math.ceil(y);
				int cx = (int) Math.ceil(x);
				int cy = (int) Math.ceil(y);

				int ax = (int) Math.floor(x);
				int ay = (int) Math.floor(y);
				int ex = (int) x;
				int ey = (int) Math.floor(y);
				int dx = (int) Math.ceil(x);
				int dy = (int) Math.floor(y);

				if (x == Math.floor(x) || y == Math.floor(y)) {
					gRed = pixel_reader.getColor((int) x, (int) y).getRed();
					gGreen = pixel_reader.getColor((int) x, (int) y).getGreen();
					gBlue = pixel_reader.getColor((int) x, (int) y).getBlue();
				} else {
					// x-axis: b -- f -- c
					double fRed = pixel_reader.getColor(bx, by).getRed()
							+ (pixel_reader.getColor(cx, cy).getRed() - pixel_reader.getColor(bx, by).getRed())
									* ((fx - bx) / (cx - bx));
					double fGreen = pixel_reader.getColor(bx, by).getGreen()
							+ (pixel_reader.getColor(cx, cy).getGreen() - pixel_reader.getColor(bx, by).getGreen())
									* ((fx - bx) / (cx - bx));
					double fBlue = pixel_reader.getColor(bx, by).getBlue()
							+ (pixel_reader.getColor(cx, cy).getBlue() - pixel_reader.getColor(bx, by).getBlue())
									* ((fx - bx) / (cx - bx));

					// x-axis: a -- e -- d
					double eRed = pixel_reader.getColor(ax, ay).getRed()
							+ (pixel_reader.getColor(dx, dy).getRed() - pixel_reader.getColor(ax, ay).getRed())
									* ((ex - ax) / (dx - ax));
					double eGreen = pixel_reader.getColor(ax, ay).getGreen()
							+ (pixel_reader.getColor(dx, dy).getGreen() - pixel_reader.getColor(ax, ay).getGreen())
									* ((ex - ax) / (dx - ax));
					double eBlue = pixel_reader.getColor(ax, ay).getBlue()
							+ (pixel_reader.getColor(dx, dy).getBlue() - pixel_reader.getColor(ax, ay).getBlue())
									* ((ex - ax) / (dx - ax));

					// y-axis: f -- g -- e
					gRed = fRed + (eRed - fRed) * ((y - fy) / (ey - fy));
					gGreen = fGreen + (eGreen - fGreen) * ((y - fy) / (ey - fy));
					gBlue = fBlue + (eBlue - fBlue) * ((y - fy) / (ey - fy));
				}
				for (c = 0; c < 3; c++) {
					new_image_writer.setColor((int) i, (int) j, Color.color(gRed, gGreen, gBlue, 1.0));
				}
			}
		}
	}

	public void clear(WritableImage image) {
		int i, j, c, w = (int) image.getWidth(), h = (int) image.getHeight();
		PixelWriter image_writer = image.getPixelWriter();

		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				for (c = 0; c < 3; c++) {
					image_writer.setColor((int) j, (int) i, colour);
				}
			}
		}
	}

	public static void main(String[] args) {
		launch();
	}

}