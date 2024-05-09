package org.narol.reactnative.tencentasr;

import androidx.annotation.NonNull;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.narol.reactnative.tencentasr.module.FlashFileRecognizerModule;
import org.narol.reactnative.tencentasr.module.OneSentenceRecognizerModule;
import org.narol.reactnative.tencentasr.module.RealTimeRecognizerModule;

public class TencentAsrPackage implements ReactPackage {
  @NonNull
  @Override
  public List<NativeModule>
  createNativeModules(@NonNull ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new FlashFileRecognizerModule(reactContext));
    modules.add(new OneSentenceRecognizerModule(reactContext));
    modules.add(new RealTimeRecognizerModule(reactContext));
    return modules;
  }

  @NonNull
  @Override
  public List<ViewManager>
  createViewManagers(@NonNull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}
