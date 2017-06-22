#import "UIImage+CMSampleBuffer.h"

@implementation UIImage (CMSampleBuffer)

+ (UIImage *)imageWithSampleBuffer:(CMSampleBufferRef)sampleBuffer {
  CVImageBufferRef imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer);
  if (!imageBuffer) {
    return nil;
  }
  
  CVPixelBufferLockBaseAddress(imageBuffer, 0);
  
  void *baseAddress = CVPixelBufferGetBaseAddress(imageBuffer);
  size_t width = CVPixelBufferGetWidth(imageBuffer);
  size_t height = CVPixelBufferGetHeight(imageBuffer);
  size_t bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
  CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
  
  CGContextRef context = CGBitmapContextCreate(
    baseAddress,
    width,
    height,
    8,
    bytesPerRow,
    colorSpace, kCGBitmapByteOrder32Little | kCGImageAlphaPremultipliedFirst
  );
  
  UIImage *image = [[UIImage alloc] initWithCGImage:CGBitmapContextCreateImage(context)];
  
  CGColorSpaceRelease(colorSpace);
  CVPixelBufferUnlockBaseAddress(imageBuffer, 0);
  CGContextRelease(context);
  
  return image;
}

@end
