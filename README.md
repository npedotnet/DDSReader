# DDSReader
DDS image reader for Java and C.

![alt text](http://3dtech.jp/wiki/index.php?plugin=attach&refer=DDSReader&openfile=DDSReader.png "DDSReader")

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
	int *pixels = ddsRead(buffer, DDS_READER_ABGR);
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

	        int [] textures = new int[1];
	        gl.glGenTextures(1, textures, 0);

	        gl.glEnable(GL.TEXTURE_2D);
	        gl.glBindTexture(GL.TEXTURE_2D, textures[0]);
	        gl.glPixelStorei(GL.UNPACK_ALIGNMENT, 4);

	        IntBuffer texBuffer = IntBuffer.wrap(pixels);
	        gl.glTexImage2D(GL.TEXTURE_2D, 0, GL.RGBA, width, height, 0, GL.RGBA, GL.UNSIGNED_BYTE, texBuffer);

	        gl.glTexParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_S, GL.REPEAT);
	        gl.glTexParameteri(GL.TEXTURE_2D, GL.TEXTURE_WRAP_T, GL.REPEAT);
	        gl.glTexParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.LINEAR);
	        gl.glTexParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.LINEAR);
	        
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
	        
	        int [] pixels = DDSReader.read(buffer, DDSReader.ABGR, 0);
	        int width = DDSReader.getWidth(buffer);
	        int height = DDSReader.getHeight(buffer);
	        
	        int [] textures = new int[1];
	        gl.glGenTextures(1, textures, 0);
	        
	        gl.glEnable(GL_TEXTURE_2D);
	        gl.glBindTexture(GL_TEXTURE_2D, textures[0]);
	        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

	        IntBuffer texBuffer = IntBuffer.wrap(pixels);
	        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, texBuffer);
	        
	        gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	        gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	        gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	        gl.glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	        
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
	        int *pixels = ddsRead(buffer, DDS_READER_ABGR);
	        
	        ddsFree(buffer);
	        
	        glGenTextures(1, &texture);
	        glEnable(GL_TEXTURE_2D);
	        glBindTexture(GL_TEXTURE_2D, texture);
	        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
	        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
	        
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	        
	        ddsFree(pixels);
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
	        int *pixels = ddsRead(buffer, DDS_READER_ABGR);
	        
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
