import 'dart:async';
import 'package:flutter_accessibility_service/flutter_accessibility_service.dart';
import 'package:flutter_accessibility_service/constants.dart';
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
  dynamic _lastAccessibilityEvent; // Lưu event gần nhất để thực hiện actions

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
      _lastAccessibilityEvent = event; // Lưu event để sử dụng sau
      _currentPackage = event.packageName;

      // Tạo thông tin màn hình hiện tại
      _currentScreenInfo = {
        'packageName': event.packageName,
        'eventType': event.eventType?.toString(),
        'isActive': event.isActive ?? false,
        'isFocused': event.isFocused ?? false,
        'screenBounds': _parseScreenBounds(event.screenBounds),
        'capturedText': event.capturedText ?? '',
        'windowType': event.windowType?.toString(),
        'timestamp': DateTime.now().toIso8601String(),
      };

      // Cập nhật danh sách elements từ subNodes và nodesText
      _currentElements = [];

      // Thêm element chính từ event
      if (event.capturedText != null && event.capturedText!.isNotEmpty) {
        _currentElements.add({
          'id': 'main_element',
          'text': event.capturedText,
          'nodeId': event.nodeId,
          'index': 0,
          'type': 'main_element',
          'isClickable': event.isClickable ?? false,
          'isEditable': event.isEditable ?? false,
          'isScrollable': event.isScrollable ?? false,
          'actions': event.actions?.map((a) => a.toString()).toList() ?? [],
        });
      }

      // Thêm elements từ subNodes
      if (event.subNodes != null && event.subNodes is List) {
        final subNodes = event.subNodes as List;
        for (int i = 0; i < subNodes.length; i++) {
          final node = subNodes[i];
          if (node != null && node.text != null && node.text!.isNotEmpty) {
            _currentElements.add({
              'id': 'sub_element_$i',
              'text': node.text,
              'nodeId': node.nodeId,
              'index': i + 1,
              'type': 'sub_element',
              'isClickable': node.isClickable ?? false,
              'isEditable': node.isEditable ?? false,
              'isScrollable': node.isScrollable ?? false,
              'actions': node.actions?.map((a) => a.toString()).toList() ?? [],
            });
          }
        }
      }

      // Thêm elements từ nodesText nếu có
      if (event.nodesText != null && event.nodesText is List) {
        final nodesText = event.nodesText as List;
        for (int i = 0; i < nodesText.length; i++) {
          if (nodesText[i] != null && nodesText[i].toString().isNotEmpty) {
            // Kiểm tra xem text này đã có trong elements chưa
            final existingElement = _currentElements.firstWhere(
              (element) => element['text'] == nodesText[i].toString(),
              orElse: () => {},
            );

            if (existingElement.isEmpty) {
              _currentElements.add({
                'id': 'text_element_$i',
                'text': nodesText[i].toString(),
                'nodeId': null,
                'index': _currentElements.length,
                'type': 'text_element',
                'isClickable': false,
                'isEditable': false,
                'isScrollable': false,
                'actions': [],
              });
            }
          }
        }
      }

      _logger.i('Updated current screen: ${event.packageName} with ${_currentElements.length} elements');
      if (_currentElements.isNotEmpty) {
        _logger.d('Elements found: ${_currentElements.map((e) => e['text']).join(', ')}');
      }
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

  /// Lấy danh sách elements có thể click
  List<Map<String, dynamic>> getClickableElements() {
    return _currentElements.where((element) => element['isClickable'] == true).toList();
  }

  /// Lấy danh sách elements có thể edit
  List<Map<String, dynamic>> getEditableElements() {
    return _currentElements.where((element) => element['isEditable'] == true).toList();
  }

  /// Lấy danh sách elements có thể scroll
  List<Map<String, dynamic>> getScrollableElements() {
    return _currentElements.where((element) => element['isScrollable'] == true).toList();
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
      final nodeId = element['nodeId'];
      final isClickable = element['isClickable'] ?? false;

      _logger.i('Attempting to click element: ${element['text']} (clickable: $isClickable)');

      if (nodeId == null) {
        _logger.w('Element has no nodeId, cannot perform click action');
        return false;
      }

      if (!isClickable) {
        _logger.w('Element is not clickable');
        return false;
      }

      // Thực hiện click action với nodeId thật
      final result = await FlutterAccessibilityService.performAction(
        nodeId,
        NodeAction.actionClick,
      );

      _logger.i('Click action result: $result for element: ${element['text']}');
      return result;

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
