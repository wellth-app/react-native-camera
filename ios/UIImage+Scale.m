#import <UIKit/UIKit.h>
#import "UIImage+Scale.h"

@implementation UIImage (Scale)

- (UIImage *)scaledToWidthPercentage:(CGFloat)widthPercentage heightPercentage:(CGFloat)heightPercentage {
    CGFloat scaledWidth = self.size.width * widthPercentage;
    CGFloat scaledHeight = self.size.height * heightPercentage;
    CGRect destinationRect = CGRectMake(0, 0, scaledWidth, scaledHeight);
    
    UIGraphicsBeginImageContext(CGSizeMake(scaledWidth, scaledHeight));
    [self drawInRect:destinationRect];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

@end
