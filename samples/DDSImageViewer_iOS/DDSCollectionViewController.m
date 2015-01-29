//
//  DDSCollectionViewController.m
//  DDSImageViewer_iOS
//
// Copyright (c) 2015 Kenji Sasaki
// Released under the MIT license.
// https://github.com/npedotnet/DDSReader/blob/master/LICENSE
//
// English document
// https://github.com/npedotnet/DDSReader/blob/master/README.md
//
// Japanese document
// http://3dtech.jp/wiki/index.php?DDSReader
//

#import "DDSCollectionViewController.h"
#include "dds_reader.h"

@interface DDSCollectionViewController () {
    NSArray *images;
}
@end

@implementation DDSCollectionViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    images = [NSArray arrayWithObjects:
              @"dds_DXT1",
              @"dds_DXT2",
              @"dds_DXT3",
              @"dds_DXT4",
              @"dds_DXT5",
              @"dds_A1R5G5B5",
              @"dds_X1R5G5B5",
              @"dds_A4R4G4B4",
              @"dds_X4R4G4B4",
              @"dds_R5G6B5",
              @"dds_A8B8G8R8",
              @"dds_X8B8G8R8",
              @"dds_A8R8G8B8",
              @"dds_X8R8G8B8",
              nil];

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return images.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    
    UICollectionViewCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:@"Cell" forIndexPath:indexPath];
    
    UIImageView *imageView = (UIImageView *)[cell viewWithTag:100];
    NSString *path = [[NSBundle mainBundle] pathForResource:[images objectAtIndex:indexPath.row] ofType:@"dds"];
    imageView.image = [self createDDSImage:path];
    
    return cell;

}

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

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
