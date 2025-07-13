import 'dart:io';
import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'api/api_server.dart';
import 'services/accessibility_service.dart';

final Logger logger = Logger();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Khởi động API server
  try {
    await ApiServer().start(port: 8080);
    logger.i('Accessibility Service API started successfully');
  } catch (e) {
    logger.e('Failed to start API server: $e');
  }

  runApp(const MyApp());
}

@pragma("vm:entry-point")
void accessibilityOverlay() {
  runApp(const MaterialApp(
    debugShowCheckedModeBanner: false,
    home: Material(
      child: Center(
        child: Text(
          "Accessibility Service Overlay",
          style: TextStyle(fontSize: 18, color: Colors.white),
        ),
      ),
      color: Colors.black54,
    ),
  ));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Screen Reading API',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.light,
        ),
        useMaterial3: true,
        cardTheme: const CardThemeData(
          elevation: 1,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(12)),
          ),
        ),
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
        cardTheme: const CardThemeData(
          elevation: 1,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(12)),
          ),
        ),
      ),
      themeMode: ThemeMode.system,
      home: const MyHomePage(title: 'Screen Reading API'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  final AccessibilityServiceManager _accessibilityService = AccessibilityServiceManager();
  String _status = 'Checking...';
  bool _isServerRunning = true;
  String _networkInfo = 'Getting network info...';

  @override
  void initState() {
    super.initState();
    _checkStatus();
    _getNetworkInfo();
  }

  Future<void> _checkStatus() async {
    final status = await _accessibilityService.getStatus();
    setState(() {
      _status = 'Accessibility: ${status.status}\nServer: ${_isServerRunning ? "Running on :8080" : "Stopped"}';
    });
  }

  Future<void> _getNetworkInfo() async {
    try {
      final interfaces = await NetworkInterface.list();
      String networkInfo = 'Network Access:\n';

      for (var interface in interfaces) {
        for (var addr in interface.addresses) {
          if (addr.type == InternetAddressType.IPv4 && !addr.isLoopback) {
            networkInfo += '• http://${addr.address}:8080\n';
          }
        }
      }

      if (networkInfo == 'Network Access:\n') {
        networkInfo += '• Only localhost available';
      }

      setState(() {
        _networkInfo = networkInfo;
      });
    } catch (e) {
      setState(() {
        _networkInfo = 'Error getting network info: $e';
      });
    }
  }

  Future<void> _requestPermission() async {
    final granted = await _accessibilityService.requestPermission();
    if (granted) {
      _checkStatus();
    }
  }

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      backgroundColor: colorScheme.surface,
      appBar: AppBar(
        backgroundColor: colorScheme.surfaceContainer,
        title: Text(
          widget.title,
          style: TextStyle(
            color: colorScheme.onSurface,
            fontWeight: FontWeight.w600,
          ),
        ),
        centerTitle: true,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Status Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          Icons.accessibility_new,
                          color: colorScheme.primary,
                          size: 24,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Status',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: colorScheme.surfaceContainerHighest,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        _status,
                        style: TextStyle(
                          fontSize: 13,
                          color: colorScheme.onSurfaceVariant,
                          height: 1.3,
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: FilledButton.icon(
                            onPressed: _requestPermission,
                            icon: const Icon(Icons.security, size: 18),
                            label: const Text('Permission', style: TextStyle(fontSize: 13)),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: OutlinedButton.icon(
                            onPressed: () {
                              _checkStatus();
                              _getNetworkInfo();
                            },
                            icon: const Icon(Icons.refresh, size: 18),
                            label: const Text('Refresh', style: TextStyle(fontSize: 13)),
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Network Info Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          Icons.wifi,
                          color: colorScheme.primary,
                          size: 24,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'Network',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: colorScheme.surfaceContainerHighest,
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        _networkInfo,
                        style: TextStyle(
                          fontSize: 12,
                          fontFamily: 'monospace',
                          color: colorScheme.onSurfaceVariant,
                          height: 1.3,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 16),

            // API Endpoints Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(
                          Icons.api,
                          color: colorScheme.primary,
                          size: 24,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          'API Endpoints',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    _buildApiEndpoint(
                      context,
                      'GET',
                      '/api/status',
                      'Check accessibility status',
                      Icons.health_and_safety,
                    ),
                    _buildApiEndpoint(
                      context,
                      'GET',
                      '/api/screen/current',
                      'Get current screen info',
                      Icons.screen_share,
                    ),
                    _buildApiEndpoint(
                      context,
                      'GET',
                      '/api/screen/elements',
                      'Get screen elements',
                      Icons.view_list,
                    ),
                    _buildApiEndpoint(
                      context,
                      'POST',
                      '/api/screen/click',
                      'Click element by index',
                      Icons.touch_app,
                    ),
                    _buildApiEndpoint(
                      context,
                      'POST',
                      '/api/screen/tap',
                      'Tap at coordinates',
                      Icons.ads_click,
                    ),
                    _buildApiEndpoint(
                      context,
                      'POST',
                      '/api/action',
                      'Global actions (back, home)',
                      Icons.keyboard_return,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16), // Extra padding at bottom
          ],
        ),
      ),
    );
  }

  Widget _buildApiEndpoint(
    BuildContext context,
    String method,
    String endpoint,
    String description,
    IconData icon,
  ) {
    final colorScheme = Theme.of(context).colorScheme;
    final isPost = method == 'POST';

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: colorScheme.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(6),
      ),
      child: Row(
        children: [
          Icon(
            icon,
            size: 16,
            color: colorScheme.primary,
          ),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.symmetric(
              horizontal: 6,
              vertical: 2,
            ),
            decoration: BoxDecoration(
              color: isPost ? colorScheme.errorContainer : colorScheme.primaryContainer,
              borderRadius: BorderRadius.circular(3),
            ),
            child: Text(
              method,
              style: TextStyle(
                fontSize: 9,
                fontWeight: FontWeight.bold,
                color: isPost ? colorScheme.onErrorContainer : colorScheme.onPrimaryContainer,
              ),
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  endpoint,
                  style: TextStyle(
                    fontSize: 12,
                    fontFamily: 'monospace',
                    fontWeight: FontWeight.w500,
                    color: colorScheme.onSurface,
                  ),
                ),
                Text(
                  description,
                  style: TextStyle(
                    fontSize: 11,
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _accessibilityService.dispose();
    super.dispose();
  }
}
