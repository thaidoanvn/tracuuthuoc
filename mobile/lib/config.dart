import 'dart:io';

import 'package:flutter/foundation.dart';

/// Android emulator reaches the host through 10.0.2.2.
/// The iOS Simulator uses localhost.
const String androidApiBaseUrl = 'http://10.0.2.2:8088';
const String iosApiBaseUrl = 'http://localhost:8088';

const String apiKey = 'thaidoan-tracuuthuoc-local';

String get apiBaseUrl {
  if (kIsWeb) {
    return iosApiBaseUrl;
  }
  if (Platform.isAndroid) {
    return androidApiBaseUrl;
  }
  return iosApiBaseUrl;
}
