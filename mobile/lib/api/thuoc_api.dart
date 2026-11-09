import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/thuoc.dart';

class ThuocApiException implements Exception {
  ThuocApiException(this.message);

  final String message;

  @override
  String toString() => message;
}

class ThuocApi {
  ThuocApi({required this.baseUrl, required this.apiKey, http.Client? client})
      : _client = client ?? http.Client();

  final String baseUrl;
  final String apiKey;
  final http.Client _client;

  Future<List<ThuocSummary>> search({
    required String keyword,
    required bool tenThuoc,
    required bool batDauVoi,
    required bool hoatChat,
    required bool cssx,
  }) async {
    final response = await _post('/api/thuoc/search', {
      'keyword': keyword,
      'tenThuoc': tenThuoc,
      'batDauVoi': batDauVoi,
      'hoatChat': hoatChat,
      'cssx': cssx,
    });
    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final result = body['result'] as List<dynamic>? ?? [];
    return result
        .map((item) => ThuocSummary.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<List<ThuocSummary>> suggest({
    required String keyword,
    required bool tenThuoc,
    required bool batDauVoi,
    required bool hoatChat,
    required bool cssx,
  }) async {
    final uri = Uri.parse('$baseUrl/api/thuoc/suggest').replace(
      queryParameters: {
        'keyword': keyword,
        'tenThuoc': '$tenThuoc',
        'batDauVoi': '$batDauVoi',
        'hoatChat': '$hoatChat',
        'cssx': '$cssx',
      },
    );
    final response = _check(
      await _client.get(uri, headers: _headers).timeout(const Duration(seconds: 15)),
    );
    final body = jsonDecode(response.body) as Map<String, dynamic>;
    final result = body['result'] as List<dynamic>? ?? [];
    return result
        .map((item) => ThuocSummary.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<ThuocDetail> detail(int id) async {
    final response = await _get('/api/thuoc/$id');
    return ThuocDetail.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        'X-Api-Key': apiKey,
      };

  Future<http.Response> _post(String path, Map<String, dynamic> body) async {
    return _check(
      await _client
          .post(
            Uri.parse('$baseUrl$path'),
            headers: _headers,
            body: jsonEncode(body),
          )
          .timeout(const Duration(seconds: 15)),
    );
  }

  Future<http.Response> _get(String path) async {
    return _check(
      await _client
          .get(Uri.parse('$baseUrl$path'), headers: _headers)
          .timeout(const Duration(seconds: 15)),
    );
  }

  http.Response _check(http.Response response) {
    if (response.statusCode >= 200 && response.statusCode < 300) {
      return response;
    }
    String message = 'Không tải được dữ liệu thuốc.';
    try {
      final body = jsonDecode(response.body);
      if (body is Map && body['message'] is String && body['message'] != '') {
        message = body['message'] as String;
      }
    } catch (_) {
      // Keep the fallback message when the body is not JSON.
    }
    throw ThuocApiException(message);
  }
}
