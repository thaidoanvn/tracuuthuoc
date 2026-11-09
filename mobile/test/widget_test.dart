import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:tracuuthuoc/api/thuoc_api.dart';
import 'package:tracuuthuoc/screens/search_screen.dart';

void main() {
  testWidgets('search screen shows the catalog filters', (tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: SearchScreen(
          api: ThuocApi(baseUrl: 'http://localhost:8088', apiKey: 'test-key'),
        ),
      ),
    );

    expect(find.text('Tra cứu thuốc'), findsOneWidget);
    expect(find.text('Tên thuốc'), findsOneWidget);
    expect(find.text('Bắt đầu với'), findsOneWidget);
    expect(find.text('Hoạt chất'), findsOneWidget);
    expect(find.text('CSSX Dược'), findsOneWidget);
    expect(find.text('Nhập từ khóa và bấm Tìm'), findsOneWidget);
  });
}
