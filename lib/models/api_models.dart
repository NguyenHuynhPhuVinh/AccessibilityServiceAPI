class ApiResponse<T> {
  final bool success;
  final String message;
  final T? data;
  final String? error;

  ApiResponse({
    required this.success,
    required this.message,
    this.data,
    this.error,
  });

  factory ApiResponse.success(T data, {String message = 'Success'}) {
    return ApiResponse(
      success: true,
      message: message,
      data: data,
    );
  }

  factory ApiResponse.error(String error) {
    return ApiResponse(
      success: false,
      message: 'Error',
      error: error,
    );
  }

  Map<String, dynamic> toJson(Object Function(T value) toJsonT) => {
    'success': success,
    'message': message,
    'data': data != null ? toJsonT(data as T) : null,
    'error': error,
  };
}

class AccessibilityStatus {
  final bool isEnabled;
  final bool hasPermission;
  final String status;

  AccessibilityStatus({
    required this.isEnabled,
    required this.hasPermission,
    required this.status,
  });

  factory AccessibilityStatus.fromJson(Map<String, dynamic> json) => AccessibilityStatus(
    isEnabled: json['isEnabled'] as bool,
    hasPermission: json['hasPermission'] as bool,
    status: json['status'] as String,
  );

  Map<String, dynamic> toJson() => {
    'isEnabled': isEnabled,
    'hasPermission': hasPermission,
    'status': status,
  };
}
