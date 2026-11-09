import 'package:flutter/material.dart';

import 'api/thuoc_api.dart';
import 'config.dart';
import 'screens/search_screen.dart';

void main() {
  runApp(const TraCuuThuocApp());
}

class TraCuuThuocApp extends StatelessWidget {
  const TraCuuThuocApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Tra cứu thuốc',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF123A8A),
          primary: const Color(0xFF123A8A),
        ),
        scaffoldBackgroundColor: const Color(0xFFF4F7FB),
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF123A8A),
          foregroundColor: Colors.white,
          centerTitle: true,
        ),
        cardTheme: CardThemeData(
          color: Colors.white,
          elevation: 1,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ),
      home: SearchScreen(api: ThuocApi(baseUrl: apiBaseUrl, apiKey: apiKey)),
    );
  }
}
