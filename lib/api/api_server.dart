import 'dart:convert';
import 'dart:io';
import 'package:shelf/shelf.dart';
import 'package:shelf/shelf_io.dart' as shelf_io;
import 'package:shelf_router/shelf_router.dart';
import 'package:shelf_cors_headers/shelf_cors_headers.dart';
import 'package:logger/logger.dart';
import '../services/accessibility_service.dart';
import '../models/api_models.dart';

class ApiServer {
  static final ApiServer _instance = ApiServer._internal();
  factory ApiServer() => _instance;
  ApiServer._internal();

  final Logger _logger = Logger();
  final AccessibilityServiceManager _accessibilityService = AccessibilityServiceManager();
  
  HttpServer? _server;
  int _port = 8080;

  /// Khởi động server API
  Future<void> start({int port = 8080}) async {
    _port = port;
    
    final router = Router();
    
    // API trạng thái accessibility service
    router.get('/api/status', _handleGetStatus);
    router.post('/api/permission', _handleRequestPermission);
    
    // API đọc màn hình và tương tác
    router.get('/api/screen/current', _handleGetCurrentScreen);
    router.get('/api/screen/elements', _handleGetCurrentElements);
    router.post('/api/screen/click', _handleClickElement);
    router.post('/api/screen/tap', _handleTapCoordinate);
    router.post('/api/action', _handleGlobalAction);
    
    // Middleware
    final handler = Pipeline()
        .addMiddleware(corsHeaders())
        .addMiddleware(logRequests())
        .addHandler(router);

    try {
      _server = await shelf_io.serve(handler, InternetAddress.anyIPv4, _port);
      
      // Lấy IP address của thiết bị
      final interfaces = await NetworkInterface.list();
      String? deviceIP;
      for (var interface in interfaces) {
        for (var addr in interface.addresses) {
          if (addr.type == InternetAddressType.IPv4 && !addr.isLoopback) {
            deviceIP = addr.address;
            break;
          }
        }
        if (deviceIP != null) break;
      }
      
      _logger.i('API Server started on:');
      _logger.i('  - Local: http://localhost:$_port');
      if (deviceIP != null) {
        _logger.i('  - Network: http://$deviceIP:$_port');
        _logger.i('  - Desktop can access: http://$deviceIP:$_port');
      }
      
      // Bắt đầu lắng nghe accessibility events
      _accessibilityService.startListening();
    } catch (e) {
      _logger.e('Error starting server: $e');
      rethrow;
    }
  }

  /// Dừng server
  Future<void> stop() async {
    await _server?.close();
    _accessibilityService.stopListening();
    _logger.i('API Server stopped');
  }

  /// Handler: Lấy trạng thái accessibility service
  Future<Response> _handleGetStatus(Request request) async {
    try {
      final status = await _accessibilityService.getStatus();
      final response = ApiResponse.success(status);
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data.toJson())),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<AccessibilityStatus>.error('Failed to get status: $e');
      return Response.internalServerError(
        body: jsonEncode(response.toJson((data) => data?.toJson() ?? {})),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  /// Handler: Yêu cầu quyền accessibility
  Future<Response> _handleRequestPermission(Request request) async {
    try {
      final result = await _accessibilityService.requestPermission();
      final response = ApiResponse.success({'granted': result});
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<Map<String, dynamic>>.error('Failed to request permission: $e');
      return Response.internalServerError(
        body: jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  /// Handler: Lấy thông tin màn hình hiện tại
  Future<Response> _handleGetCurrentScreen(Request request) async {
    try {
      final screenInfo = _accessibilityService.getCurrentScreen();
      final currentPackage = _accessibilityService.getCurrentPackage();
      
      final response = ApiResponse.success({
        'screen': screenInfo,
        'currentPackage': currentPackage,
        'timestamp': DateTime.now().toIso8601String(),
      });
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<Map<String, dynamic>>.error('Failed to get current screen: $e');
      return Response.internalServerError(
        body: jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  /// Handler: Lấy danh sách elements trên màn hình hiện tại
  Future<Response> _handleGetCurrentElements(Request request) async {
    try {
      final elements = _accessibilityService.getCurrentElements();
      final currentPackage = _accessibilityService.getCurrentPackage();
      
      final response = ApiResponse.success({
        'elements': elements,
        'count': elements.length,
        'currentPackage': currentPackage,
        'timestamp': DateTime.now().toIso8601String(),
      });
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<Map<String, dynamic>>.error('Failed to get current elements: $e');
      return Response.internalServerError(
        body: jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  /// Handler: Click vào element theo index
  Future<Response> _handleClickElement(Request request) async {
    try {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      final elementIndex = data['elementIndex'] as int;
      
      final result = await _accessibilityService.clickElement(elementIndex);
      final response = ApiResponse.success({
        'clicked': result,
        'elementIndex': elementIndex,
        'timestamp': DateTime.now().toIso8601String(),
      });
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<Map<String, dynamic>>.error('Failed to click element: $e');
      return Response.badRequest(
        body: jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  /// Handler: Tap vào tọa độ cụ thể
  Future<Response> _handleTapCoordinate(Request request) async {
    try {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      final x = (data['x'] as num).toDouble();
      final y = (data['y'] as num).toDouble();
      
      final result = await _accessibilityService.clickCoordinate(x, y);
      final response = ApiResponse.success({
        'tapped': result,
        'x': x,
        'y': y,
        'timestamp': DateTime.now().toIso8601String(),
      });
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<Map<String, dynamic>>.error('Failed to tap coordinate: $e');
      return Response.badRequest(
        body: jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }

  /// Handler: Thực hiện global action
  Future<Response> _handleGlobalAction(Request request) async {
    try {
      final body = await request.readAsString();
      final data = jsonDecode(body) as Map<String, dynamic>;
      final action = data['action'] as String;
      
      final result = await _accessibilityService.performGlobalAction(action);
      final response = ApiResponse.success({
        'performed': result,
        'action': action,
      });
      
      return Response.ok(
        jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    } catch (e) {
      final response = ApiResponse<Map<String, dynamic>>.error('Failed to perform action: $e');
      return Response.badRequest(
        body: jsonEncode(response.toJson((data) => data)),
        headers: {'Content-Type': 'application/json'},
      );
    }
  }
}
