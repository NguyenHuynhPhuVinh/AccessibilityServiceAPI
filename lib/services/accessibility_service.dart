import 'dart:async';
import 'package:flutter_accessibility_service/flutter_accessibility_service.dart';
import 'package:logger/logger.dart';
import '../models/api_models.dart';

class AccessibilityServiceManager {
  static final AccessibilityServiceManager _instance = AccessibilityServiceManager._internal();
  factory AccessibilityServiceManager() => _instance;
  AccessibilityServiceManager._internal();

  final Logger _logger = Logger();
  StreamSubscription? _eventSubscription;
  final StreamController<Map<String, dynamic>> _eventController = StreamController.broadcast();

  // Lưu trữ thông tin màn hình hiện tại
  Map<String, dynamic>? _currentScreenInfo;
  String? _currentPackage;
  List<Map<String, dynamic>> _currentElements = [];

  Stream<Map<String, dynamic>> get eventStream => _eventController.stream;

  /// Kiểm tra trạng thái accessibility service
  Future<AccessibilityStatus> getStatus() async {
    try {
      final isEnabled = await FlutterAccessibilityService.isAccessibilityPermissionEnabled();
      
      return AccessibilityStatus(
        isEnabled: isEnabled,
        hasPermission: isEnabled,
        status: isEnabled ? 'enabled' : 'disabled',
      );
    } catch (e) {
      _logger.e('Error getting accessibility status: $e');
      return AccessibilityStatus(
        isEnabled: false,
        hasPermission: false,
        status: 'error: $e',
      );
    }
  }

  /// Yêu cầu quyền accessibility
  Future<bool> requestPermission() async {
    try {
      final result = await FlutterAccessibilityService.requestAccessibilityPermission();
      _logger.i('Accessibility permission request result: $result');
      return result;
    } catch (e) {
      _logger.e('Error requesting accessibility permission: $e');
      return false;
    }
  }

  /// Bắt đầu lắng nghe các sự kiện accessibility
  void startListening() {
    if (_eventSubscription != null) {
      _logger.w('Already listening to accessibility events');
      return;
    }

    _eventSubscription = FlutterAccessibilityService.accessStream.listen(
      (event) {
        _logger.d('Accessibility event: ${event.eventType} - ${event.packageName}');

        // Cập nhật thông tin màn hình hiện tại
        _updateCurrentScreen(event);

        // Convert event to Map for easier handling
        final eventMap = {
          'eventType': event.eventType?.toString(),
          'packageName': event.packageName,
          'eventTime': event.eventTime?.toIso8601String(),
          'actionType': event.actionType,
          'movementGranularity': event.movementGranularity,
          'screenBounds': event.screenBounds?.toString(),
          'isActive': event.isActive,
          'isFocused': event.isFocused,
        };
        _eventController.add(eventMap);
      },
      onError: (error) {
        _logger.e('Error in accessibility stream: $error');
      },
    );

    _logger.i('Started listening to accessibility events');
  }

  /// Dừng lắng nghe các sự kiện accessibility
  void stopListening() {
    _eventSubscription?.cancel();
    _eventSubscription = null;
    _logger.i('Stopped listening to accessibility events');
  }

  /// Thực hiện global action đơn giản (chỉ back)
  Future<bool> performGlobalAction(String action) async {
    try {
      // Tạm thời chỉ hỗ trợ back action
      if (action.toLowerCase() == 'back') {
        // Sử dụng system back action thông qua intent hoặc method khác
        _logger.i('Performing back action (simulated)');
        return true;
      } else {
        _logger.w('Action $action not supported yet');
        return false;
      }
    } catch (e) {
      _logger.e('Error performing global action: $e');
      return false;
    }
  }

  /// Cập nhật thông tin màn hình hiện tại từ accessibility event
  void _updateCurrentScreen(dynamic event) {
    try {
      _currentPackage = event.packageName;

      // Tạo thông tin màn hình hiện tại
      _currentScreenInfo = {
        'packageName': event.packageName,
        'eventType': event.eventType?.toString(),
        'isActive': event.isActive ?? false,
        'isFocused': event.isFocused ?? false,
        'screenBounds': _parseScreenBounds(event.screenBounds),
        'timestamp': DateTime.now().toIso8601String(),
      };

      // Cập nhật danh sách elements nếu có nodesText
      if (event.nodesText != null && event.nodesText is List) {
        _currentElements = [];
        final nodes = event.nodesText as List;
        for (int i = 0; i < nodes.length; i++) {
          if (nodes[i] != null && nodes[i].toString().isNotEmpty) {
            _currentElements.add({
              'id': 'element_$i',
              'text': nodes[i].toString(),
              'index': i,
              'type': 'text_element',
            });
          }
        }
      }

      _logger.d('Updated current screen: ${event.packageName} with ${_currentElements.length} elements');
    } catch (e) {
      _logger.e('Error updating current screen: $e');
    }
  }

  /// Parse screen bounds từ string
  Map<String, dynamic>? _parseScreenBounds(dynamic bounds) {
    if (bounds == null) return null;

    try {
      final boundsStr = bounds.toString();
      // Parse format: "left: 0 - right: 720 - top: 0 - bottom: 1544 - width: 720 - height: 1544"
      final regex = RegExp(r'left: (\d+) - right: (\d+) - top: (\d+) - bottom: (\d+) - width: (\d+) - height: (\d+)');
      final match = regex.firstMatch(boundsStr);

      if (match != null) {
        return {
          'left': int.parse(match.group(1)!),
          'right': int.parse(match.group(2)!),
          'top': int.parse(match.group(3)!),
          'bottom': int.parse(match.group(4)!),
          'width': int.parse(match.group(5)!),
          'height': int.parse(match.group(6)!),
        };
      }
    } catch (e) {
      _logger.w('Error parsing screen bounds: $e');
    }

    return null;
  }

  /// Lấy thông tin màn hình hiện tại
  Map<String, dynamic>? getCurrentScreen() {
    return _currentScreenInfo;
  }

  /// Lấy danh sách elements trên màn hình hiện tại
  List<Map<String, dynamic>> getCurrentElements() {
    return List.from(_currentElements);
  }

  /// Lấy package hiện tại
  String? getCurrentPackage() {
    return _currentPackage;
  }

  /// Click vào element theo index
  Future<bool> clickElement(int elementIndex) async {
    try {
      if (elementIndex < 0 || elementIndex >= _currentElements.length) {
        _logger.w('Invalid element index: $elementIndex');
        return false;
      }

      final element = _currentElements[elementIndex];
      _logger.i('Attempting to click element: ${element['text']}');

      // Thực hiện click action - cần nodeId từ event
      // Đây là simplified version, có thể cần cải thiện
      _logger.i('Click action simulated for element: ${element['text']}');
      return true;

    } catch (e) {
      _logger.e('Error clicking element: $e');
      return false;
    }
  }

  /// Click vào tọa độ cụ thể trên màn hình
  Future<bool> clickCoordinate(double x, double y) async {
    try {
      _logger.i('Attempting to click at coordinates: ($x, $y)');

      // Sử dụng global action hoặc gesture để click
      // Đây là simplified version
      _logger.i('Click action simulated at coordinates: ($x, $y)');
      return true;

    } catch (e) {
      _logger.e('Error clicking coordinate: $e');
      return false;
    }
  }





  void dispose() {
    stopListening();
    _eventController.close();
  }
}
