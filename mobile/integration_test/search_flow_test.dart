import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:tracuuthuoc/main.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('search then open a drug', (tester) async {
    await tester.pumpWidget(const TraCuuThuocApp());
    await tester.enterText(find.byType(TextField), 'para');
    await tester.tap(find.widgetWithText(FilledButton, 'Tìm'));
    await tester.pumpAndSettle(const Duration(seconds: 20));

    expect(find.text('Paracetamol 500mg'), findsOneWidget);
    expect(find.text('Không tìm thấy thuốc'), findsNothing);

    await tester.tap(find.text('Paracetamol 500mg'));
    await tester.pumpAndSettle(const Duration(seconds: 20));

    expect(find.text('Giấy phép'), findsOneWidget);
    expect(find.text('VD-SAMPLE-001'), findsOneWidget);

    await tester.scrollUntilVisible(
      find.textContaining('thaidoan'),
      300,
    );
    await tester.pumpAndSettle();
    expect(find.textContaining('thaidoan'), findsOneWidget);
  });
}
