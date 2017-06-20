#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <React/RCTComponent.h>
#import "CameraFocusSquare.h"

@class RCTCameraManager;

@interface RCTCamera : UIView

@property(nonatomic, copy) RCTDirectEventBlock onCaptureOutput;

- (id)initWithManager:(RCTCameraManager *)manager bridge:(RCTBridge *)bridge;

@end
