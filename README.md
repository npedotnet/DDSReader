# DDSReader
DDS image reader for Java and C.

![alt text](http://3dtech.jp/wiki/index.php?plugin=attach&refer=DDSReader&openfile=DDSReader.png "DDSReader")

![alt text](http://3dtech.jp/wiki/index.php?plugin=attach&refer=DDSReader&openfile=ddsimagedemo.png "DDSWebCanvas")

Online DDS Canvas Demo page is http://npe-net.appspot.com/npesdk/gwt/ddsimagedemo/index.html

<a href="http://3dtech.jp/wiki/index.php?plugin=attach&refer=DDSReader&openfile=ddswebgldemo.png">
<img width=50% height=50% src="http://3dtech.jp/wiki/index.php?plugin=attach&refer=DDSReader&openfile=ddswebgldemo.png">
</a>

Online DDS WebGL Texture Demo page is http://npe-net.appspot.com/npesdk/gwt/ddswebgldemo/index.html

## License

Released under the MIT license.

https://github.com/npedotnet/DDSReader/blob/master/LICENSE

## Getting Started

### 1. Add the source code to your project.

**Java**
- Add src/java/DDSReader.java to your project, and modify package statement.

**C**
- Add src/c/dds_reader.{c,h} to your project.

### 2. Create a DDS binary data buffer.

**Java**
```java
	FileInputStream fis = new FileInputStream(new File("test.dds"));
	byte [] buffer = new byte[fis.available()];
	fis.read(buffer);
	fis.close();
```

**C**
```c
#include "dds_reader.h"

FILE *file = fopen("test.dds", "rb");
if(file) {
	int size;
	fseek(file, 0, SEEK_END);
	size = ftell(file);
	fseek(file, 0, SEEK_SET);

	unsigned char *buffer = (unsigned char *)ddsMalloc(size);
	fread(buffer, 1, size, file);
	fclose(file);
}
```

### 3. Create pixels with the RGBA byte order and mipmap parameter.

ByteOrder|Java|C|Comments
---|---|---|---
ARGB|DDSReader.ARGB|DDS_READER_ARGB|for java.awt.image.BufferedImage, android.graphics.Bitmap
ABGR|DDSReader.ABGR|DDS_READER_ABGR|for OpenGL Texture(GL_RGBA), iOS UIImage

**Java**
```java
	byte [] buffer = ...;
	int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
	int width = DDSReader.getWidth(buffer);
	int height = DDSReader.getHeight(buffer);
```

**C**
```c
	unsigned char *buffer = ...;
	int *pixels = ddsRead(buffer, DDS_READER_ABGR, 0);
	int width = ddsGetWidth(buffer);
	int height = ddsGetHeight(buffer);
```

### 4. Use created pixels in your application.

#### 4.1. Java OpenGL Application
Sample code to create Java OpenGL texture.

**Java**
```java
	public int createDDSTexture() {
		int texture = 0;
	
		try {
			FileInputStream fis = new FileInputStream(path);
			byte [] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();

			int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, 0);
			int width = DDSReader.getWidth(buffer);
			int height = DDSReader.getHeight(buffer);
			int mipmap = DDSReader.getMipmap(buffer);

			int [] textures = new int[1];
			gl.glGenTextures(1, textures, 0);

			gl.glEnable(GL.TEXTURE_2D);
			gl.glBindTexture(GL.TEXTURE_2D, textures[0]);
			gl.glPixelStorei(GL.UNPACK_ALIGNMENT, 4);

			if(mipmap > 0) {
				// mipmaps
				for(int i=0; (width > 0) || (height > 0); i++) {
					if(width <= 0) width = 1;
					if(height <= 0) height = 1;
					int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, i);
	
					IntBuffer texBuffer = IntBuffer.wrap(pixels);
					gl.glTexImage2D(TEXTURE_2D, i, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, texBuffer);
	
					width /= 2;
					height /= 2;
				}
	
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_WRAP_S, REPEAT);
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_WRAP_T, REPEAT);
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR_MIPMAP_NEAREST);
			}
			else {
				// no mipmaps
				int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, 0);
	
				IntBuffer texBuffer = IntBuffer.wrap(pixels);
				gl.glTexImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, texBuffer);
	
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_WRAP_S, REPEAT);
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_WRAP_T, REPEAT);
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_MAG_FILTER, LINEAR);
				gl.glTexParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR);

			}
	        
			texture = textures[0];
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	
		return texture;
	}
```

#### 4.2. Java Application
Sample code to create java.awt.image.BufferedImage.

**Java**
```java
    private static JLabel createDDSLabel(String path) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        byte [] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();

        int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
        int width = DDSReader.getWidth(buffer);
        int height = DDSReader.getHeight(buffer);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        ImageIcon icon = new ImageIcon(image.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH));
        return new JLabel(icon);
    }
```

For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSSwingBufferedImage

#### 4.3. Android OpenGL Application
Sample code to create Android OpenGL texture.

**Java**
```java
	import static javax.microedition.khronos.opengles.GL10;

	public int createDDSTexture(GL10 gl, String path) {
		int texture = 0;
		try {
			
			InputStream is = getContext().getAssets().open(path);
			byte [] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();
			
			int width = DDSReader.getWidth(buffer);
			int height = DDSReader.getHeight(buffer);
			int mipmap = DDSReader.getMipmap(buffer);
			
			int [] textures = new int[1];
			gl.glGenTextures(1, textures, 0);
			
			gl.glEnable(GL_TEXTURE_2D);
			gl.glBindTexture(GL_TEXTURE_2D, textures[0]);
			gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
			
			if(mipmap > 0) {
				// mipmap
				for(int i=0; (width > 0) || (height > 0); i++) {
					if(width <= 0) width = 1;
					if(height <= 0) height = 1;
					int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, i);
					
					IntBuffer texBuffer = IntBuffer.wrap(pixels);
					gl.glTexImage2D(GL_TEXTURE_2D, i, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texBuffer);
					
					width /= 2;
					height /= 2;
				}
				
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
			}
			else {
				// no mipmap
				int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, 0);
				
				IntBuffer texBuffer = IntBuffer.wrap(pixels);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texBuffer);
				
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			}
			
			texture = textures[0];
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return texture;
	}
```

For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSGLViewer_Android

#### 4.4. Android Application
Sample code to create android.graphics.Bitmap.

**Java**
```Java
    private Bitmap createDDSBitmap(String path) {
        Bitmap bitmap = null;
        try {
            InputStream is = getAssets().open(path);
            byte [] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            
            int [] pixels = DDSReader.read(buffer, DDSReader.ARGB, 0);
            int width = DDSReader.getWidth(buffer);
            int height = DDSReader.getHeight(buffer);
            
            bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Config.ARGB_8888);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
```
For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSBitmapViewer_Android

#### 4.5. iOS OpenGL Application
Sample code to create iOS OpenGL texture.

**Objective-C**
```objc
- (GLuint)createDDSTexture:(NSString *)path {
    
    GLuint texture = 0;
    
    FILE *file = fopen([path UTF8String], "rb");
    if(file) {
        fseek(file, 0, SEEK_END);
        int size = ftell(file);
        fseek(file, 0, SEEK_SET);
        
        unsigned char *buffer = (unsigned char *)ddsMalloc(size);
        fread(buffer, 1, size, file);
        fclose(file);
        
        int width = ddsGetWidth(buffer);
        int height = ddsGetHeight(buffer);
        int mipmap = ddsGetMipmap(buffer);
        
        glGenTextures(1, &texture);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, texture);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        
        if(mipmap > 0) {
            // mipmap
            for(int i=0; (width > 0) || (height > 0); i++) {
                if(width <= 0) width = 1;
                if(height <= 0) height = 1;
                int *pixels = ddsRead(buffer, DDS_READER_ABGR, i);
                
                glTexImage2D(GL_TEXTURE_2D, i, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

                width /= 2;
                height /= 2;

                ddsFree(pixels);
            }
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
        }
        else {
            // no mipmap
            int *pixels = ddsRead(buffer, DDS_READER_ABGR, 0);
            
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            
            ddsFree(pixels);
        }
        
        ddsFree(buffer);
    }
    
    return texture;
    
}
```

For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSGLViewer_iOS

#### 4.6. iOS Application
Sample code to create iOS UIImage.

**Objective-C**
```objc
- (UIImage *)createDDSImage:(NSString *)path {
	    
    FILE *file = fopen([path UTF8String], "rb");
    if(file) {
        fseek(file, 0, SEEK_END);
        int size = ftell(file);
        fseek(file, 0, SEEK_SET);
        
        unsigned char *buffer = (unsigned char *)ddsMalloc(size);
        fread(buffer, 1, size, file);
        fclose(file);
        
        int width = ddsGetWidth(buffer);
        int height = ddsGetHeight(buffer);
        int *pixels = ddsRead(buffer, DDS_READER_ABGR, 0);
        
        ddsFree(buffer);
        
        CGColorSpaceRef colorSpaceRef = CGColorSpaceCreateDeviceRGB();
        CGBitmapInfo bitmapInfo = (CGBitmapInfo)kCGImageAlphaLast;
        CGDataProviderRef providerRef = CGDataProviderCreateWithData(NULL, pixels, 4*width*height, releaseDataCallback);
	        
        CGImageRef imageRef = CGImageCreate(width, height, 8, 32, 4*width, colorSpaceRef, bitmapInfo, providerRef, NULL, 0, kCGRenderingIntentDefault);
	        
        UIImage *image = [[UIImage alloc] initWithCGImage:imageRef];
	        
        CGColorSpaceRelease(colorSpaceRef);
	        
        return image;
    }
	    
    return nil;
	    
}

static void releaseDataCallback(void *info, const void *data, size_t size) {
	ddsFree((void *)data);
}
```
For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSImageViewer_iOS

#### 4.7. GWT Web Application

Sample code to create DDS HTML5 Canvas with GWT.

**Java**
```java
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.CanvasPixelArray;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.gwt.xhr.client.XMLHttpRequest.ResponseType;

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
	
	return canvas;
	
}

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
```

For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSWebViewer_GWT

#### 4.8 GWT WebGL(GwtGL) Application

Sample code to create DDS WebGL texture with GWT.

```java
import com.google.gwt.canvas.client.Canvas;
import com.googlecode.gwtgl.binding.WebGLRenderingContext;
import com.googlecode.gwtgl.binding.WebGLTexture;

import static com.googlecode.gwtgl.binding.WebGLRenderingContext.*;

WegGLRenderingContext gl = ...;

// See 4.7. GWT Web Application
Canvas canvas = createImageCanvas(pixels, width, height);

WebGLTexture texture = gl.createTexture();

gl.enable(TEXTURE_2D);
gl.bindTexture(TEXTURE_2D, texture);
gl.texImage2D(TEXTURE_2D, 0, RGBA, RGBA, UNSIGNED_BYTE, canvas.getElement());
```

For more details, please refer to the sample project.

https://github.com/npedotnet/DDSReader/tree/master/samples/DDSWebGLViewer_GWT

### 5. Free allocated memory (C language Only)

**C**
```c
	unsigned char *buffer = ...;
	int *pixels = ddsRead(buffer, DDS_READER_ABGR, 0);
	if(pixels) {
		ddsFree(pixels);
	}
	ddsFree(buffer);
```

## Memory Management (C language Only)

If you have your memory management system, please customize ddsMalloc() and ddsFree().

**C**
```c
	void *ddsMalloc(size_t size) {
		return malloc(size);
	}

	void ddsFree(void *memory) {
		free(memory);
	}
```

## Supported
- Mipmaps
- DXT compressed images (DXT1, DXT2, DXT3, DXT4, DXT5)
- 16bit RGB images (A1R5G5B5, X1R5G5B5, A4R4G4B4, X4R4G4B4, R5G6B5)
- 24bit RGB image (R8G8B8)
- 32bit RGB images (A8B8G8R8, X8B8G8R8, A8R8G8B8, X8R8G8B8)

## Unsupported
- Other RGB images
- Luminance images
- YUV images
- Floating-Point images


Thank you for reading through. Enjoy your programming life!
