#ifdef RCT_NEW_ARCH_ENABLED
#import "RNTencentAsrSpec.h"

@interface TencentAsr : NSObject <NativeTencentAsrSpec>
#else
#import <React/RCTBridgeModule.h>

@interface TencentAsr : NSObject <RCTBridgeModule>
#endif

@end
