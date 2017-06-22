#import <UIKit/UIKit.h>
#import <CoreMedia/CoreMedia.h>

@interface UIImage (CMSampleBuffer)

+ (UIImage *)imageWithSampleBuffer:(CMSampleBufferRef)sampleBuffer;

@end
