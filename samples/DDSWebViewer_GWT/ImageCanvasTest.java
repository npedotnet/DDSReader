package net.npe.imagecanvastest.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.gwt.xhr.client.XMLHttpRequest.ResponseType;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ImageCanvasTest implements EntryPoint {
	
	private static String [] DDS_FILES = {
		"dds_A1R5G5B5.dds",
		"dds_A4R4G4B4.dds",
		"dds_A8B8G8R8.dds",
		"dds_A8R8G8B8.dds",
		"dds_DXT1.dds",
		"dds_DXT2.dds",
		"dds_DXT3.dds",
		"dds_DXT4.dds",
		"dds_DXT5.dds",
		"dds_R5G6B5.dds",
		"dds_R8G8B8.dds",
		"dds_X1R5G5B5.dds",
		"dds_X4R4G4B4.dds",
		"dds_X8B8G8R8.dds",
		"dds_X8R8G8B8.dds",
	};

	public void onModuleLoad() {
		
		panel = new FlowPanel();
		panel.getElement().getStyle().setBackgroundColor("orange");

		for(int i=0; i<DDS_FILES.length; i++) {
			addDDSCanvas("images/"+DDS_FILES[i]);
		}
		
		RootLayoutPanel.get().add(new ScrollPanel(panel));
		
	}
	
	private FlowPanel panel;
	
	private void addDDSCanvas(String url) {
		XMLHttpRequest request = XMLHttpRequest.create();
		request.open("GET", url);
		request.setResponseType(ResponseType.ArrayBuffer);
		request.setOnReadyStateChange(new ReadyStateChangeHandler() {
			@Override
			public void onReadyStateChange(XMLHttpRequest xhr) {
				if(xhr.getReadyState() == XMLHttpRequest.DONE) {
					if(xhr.getStatus() >= 400) {
						// error
						System.out.println("Error");
					}
					else {
						try {
							ArrayBuffer arrayBuffer = xhr.getResponseArrayBuffer();
							Uint8ArrayNative u8array = Uint8ArrayNative.create(arrayBuffer);
							byte [] buffer = new byte[u8array.length()];
							for(int i=0; i<buffer.length; i++) {
								buffer[i] = (byte)u8array.get(i);
							}
							int pixels [] = DDSReader.read(buffer, DDSReader.ABGR, 0);
							int width = DDSReader.getWidth(buffer);
							int height = DDSReader.getHeight(buffer);
							
							Canvas canvas = createImageCanvas(pixels, width, height);
							panel.add(canvas);
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		request.send();
	}
	
	private Canvas createImageCanvas(int [] pixels, int width, int height) {
		
		Canvas canvas = Canvas.createIfSupported();
		canvas.setCoordinateSpaceWidth(width);
		canvas.setCoordinateSpaceHeight(height);
		
		Context2d context = canvas.getContext2d();
		ImageData data = context.createImageData(width, height);

		CanvasPixelArray array = data.getData();
		for(int i=0; i<width*height; i++) { // ABGR
			array.set(4*i+0, pixels[i] & 0xFF);
			array.set(4*i+1, (pixels[i] >> 8) & 0xFF);
			array.set(4*i+2, (pixels[i] >> 16) & 0xFF);
			array.set(4*i+3, (pixels[i] >> 24) & 0xFF);
		}
		context.putImageData(data, 0, 0);
		
		canvas.getElement().getStyle().setMargin(4, Unit.PX);
		
		return canvas;
		
	}
	
}
