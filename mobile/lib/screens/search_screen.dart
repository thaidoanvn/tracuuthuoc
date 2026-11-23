import 'dart:async';

import 'package:flutter/material.dart';

import '../api/thuoc_api.dart';
import '../models/thuoc.dart';
import 'detail_screen.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key, required this.api});

  final ThuocApi api;

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _keyword = TextEditingController();
  bool _tenThuoc = true;
  bool _allFields = true;
  bool _hoatChat = true;
  bool _cssx = true;
  bool _loading = false;
  bool _searched = false;
  bool _applyingSuggestion = false;
  String? _error;
  List<ThuocSummary> _results = [];
  List<ThuocSummary> _suggestions = [];
  Map<String, bool> _display = {};
  Timer? _suggestTimer;
  int _suggestRequest = 0;

  @override
  void initState() {
    super.initState();
    _keyword.addListener(_onKeywordChanged);
    _loadDisplay();
  }

  Future<void> _loadDisplay() async {
    try {
      final fields = await widget.api.displayFields();
      if (mounted) {
        setState(() => _display = fields);
      }
    } catch (_) {
      // Keep showing every field when the setting cannot be loaded.
    }
  }

  bool _shows(String key) => _display[key] != false;

  @override
  void dispose() {
    _suggestTimer?.cancel();
    _keyword.removeListener(_onKeywordChanged);
    _keyword.dispose();
    super.dispose();
  }

  void _onKeywordChanged() {
    if (_applyingSuggestion) {
      return;
    }
    _scheduleSuggest();
  }

  void _scheduleSuggest() {
    _suggestTimer?.cancel();
    final keyword = _keyword.text.trim();
    if (keyword.length < 3) {
      setState(() => _suggestions = []);
      return;
    }
    _suggestTimer = Timer(const Duration(milliseconds: 300), () {
      _loadSuggestions(keyword);
    });
  }

  Future<void> _loadSuggestions(String keyword) async {
    final requestId = ++_suggestRequest;
    try {
      final suggestions = await widget.api.suggest(
        keyword: keyword,
        tenThuoc: _tenThuoc,
        allFields: _allFields,
        hoatChat: _hoatChat,
        cssx: _cssx,
      );
      if (!mounted || requestId != _suggestRequest || _keyword.text.trim() != keyword) {
        return;
      }
      setState(() => _suggestions = suggestions);
    } catch (_) {
      if (!mounted || requestId != _suggestRequest) {
        return;
      }
      setState(() => _suggestions = []);
    }
  }

  void _selectSuggestion(ThuocSummary drug) {
    _suggestTimer?.cancel();
    _suggestRequest++;
    _applyingSuggestion = true;
    _keyword.text = drug.tenThuoc;
    _keyword.selection = TextSelection.collapsed(offset: _keyword.text.length);
    _applyingSuggestion = false;
    setState(() => _suggestions = []);
    _search();
  }

  Future<void> _search() async {
    setState(() {
      _loading = true;
      _error = null;
      _searched = true;
      _suggestions = [];
    });
    try {
      final results = await widget.api.search(
        keyword: _keyword.text,
        tenThuoc: _tenThuoc,
        allFields: _allFields,
        hoatChat: _hoatChat,
        cssx: _cssx,
      );
      if (!mounted) {
        return;
      }
      Map<String, bool> fields = _display;
      try {
        fields = await widget.api.displayFields();
      } catch (_) {}
      if (!mounted) {
        return;
      }
      setState(() {
        _display = fields;
        _results = results;
        _loading = false;
      });
    } on ThuocApiException catch (error) {
      if (!mounted) {
        return;
      }
      setState(() {
        _results = [];
        _error = error.message;
        _loading = false;
      });
    } catch (_) {
      if (!mounted) {
        return;
      }
      setState(() {
        _results = [];
        _error = 'Không kết nối được máy chủ. Kiểm tra API đang chạy.';
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Tra cứu thuốc')),
      body: Column(
        children: [
          Material(
            color: const Color(0xFF123A8A),
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
              child: Column(
                children: [
                  TextField(
                    controller: _keyword,
                    style: const TextStyle(color: Colors.white),
                    cursorColor: Colors.white,
                    textInputAction: TextInputAction.search,
                    onSubmitted: (_) => _search(),
                    decoration: InputDecoration(
                      hintText: 'Tìm...',
                      hintStyle: const TextStyle(color: Colors.white70),
                      filled: true,
                      fillColor: Colors.white24,
                      suffixIcon: IconButton(
                        onPressed: _loading ? null : _search,
                        icon: const Icon(Icons.search, color: Colors.white),
                        tooltip: 'Tìm',
                      ),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(28),
                        borderSide: BorderSide.none,
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Wrap(
                    alignment: WrapAlignment.center,
                    spacing: 4,
                    children: [
                      _filter('Tên thuốc', _tenThuoc, (value) {
                        setState(() => _tenThuoc = value);
                        _scheduleSuggest();
                      }),
                      _filter('Tất cả trường', _allFields, (value) {
                        setState(() => _allFields = value);
                        _scheduleSuggest();
                      }),
                      _filter('Hoạt chất', _hoatChat, (value) {
                        setState(() => _hoatChat = value);
                        _scheduleSuggest();
                      }),
                      _filter('CSSX Dược', _cssx, (value) {
                        setState(() => _cssx = value);
                        _scheduleSuggest();
                      }),
                    ],
                  ),
                  const SizedBox(height: 8),
                  FilledButton(
                    onPressed: _loading ? null : _search,
                    style: FilledButton.styleFrom(
                      backgroundColor: const Color(0xFF66BB6A),
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Tìm'),
                  ),
                ],
              ),
            ),
          ),
          if (_suggestions.isNotEmpty) _suggestionList(),
          Expanded(child: _resultsBody()),
        ],
      ),
    );
  }

  Widget _filter(String label, bool value, ValueChanged<bool> onChanged) {
    return FilterChip(
      label: Text(label),
      selected: value,
      onSelected: onChanged,
      showCheckmark: true,
      checkmarkColor: Colors.white,
      labelStyle: const TextStyle(color: Colors.white),
      side: BorderSide(color: value ? const Color(0xFF66BB6A) : Colors.white54),
      color: WidgetStateProperty.resolveWith((states) {
        if (states.contains(WidgetState.selected)) {
          return const Color(0xFF66BB6A);
        }
        return Colors.transparent;
      }),
    );
  }

  Widget _suggestionList() {
    return Material(
      color: Colors.white,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxHeight: 220),
        child: ListView.separated(
          shrinkWrap: true,
          itemCount: _suggestions.length,
          separatorBuilder: (_, _) => const Divider(height: 1),
          itemBuilder: (context, index) {
            final drug = _suggestions[index];
            return ListTile(
              dense: true,
              title: Text(drug.tenThuoc),
              subtitle: Text([
                if (_shows('code') && drug.code.isNotEmpty) drug.code,
                if (_shows('hoatChat') && drug.hoatChat.isNotEmpty) drug.hoatChat,
              ].join(' · ')),
              onTap: () => _selectSuggestion(drug),
            );
          },
        ),
      ),
    );
  }

  Widget _resultsBody() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_error != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline, size: 48, color: Color(0xFF123A8A)),
              const SizedBox(height: 12),
              Text(_error!, textAlign: TextAlign.center),
              const SizedBox(height: 16),
              FilledButton(onPressed: _search, child: const Text('Thử lại')),
            ],
          ),
        ),
      );
    }
    if (!_searched) {
      return const Center(
        child: Text('Nhập từ khóa và bấm Tìm'),
      );
    }
    if (_results.isEmpty) {
      return const Center(child: Text('Không tìm thấy thuốc'));
    }
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: _results.length,
      separatorBuilder: (_, _) => const SizedBox(height: 12),
      itemBuilder: (context, index) {
        final drug = _results[index];
        return Card(
          child: InkWell(
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute<void>(
                  builder: (_) => DetailScreen(api: widget.api, id: drug.id),
                ),
              );
            },
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    drug.tenThuoc,
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: const Color(0xFF123A8A),
                          fontWeight: FontWeight.w700,
                        ),
                  ),
                  const SizedBox(height: 6),
                  if (_shows('code')) Text('Mã: ${drug.code}'),
                  if (_shows('hoatChat'))
                    Text('Hoạt chất: ${drug.hoatChat}', maxLines: 2, overflow: TextOverflow.ellipsis),
                  if (_shows('dangBaoChe'))
                    Text('Dạng bào chế: ${drug.dangBaoChe}', maxLines: 2, overflow: TextOverflow.ellipsis),
                  if (_shows('hangSanXuat'))
                    Text('Hãng sản xuất: ${drug.hangSanXuat}', maxLines: 2, overflow: TextOverflow.ellipsis),
                  if (_shows('hoiChuan')) Text('Hội chẩn: ${displayOrKhong(drug.hoiChuan)}'),
                  if (_shows('kiemSoatDacBiet'))
                    Text('Kiểm soát đặc biệt: ${displayOrKhong(drug.kiemSoatDacBiet)}'),
                  if (_shows('bhytTyLe') || _shows('donGia')) const SizedBox(height: 8),
                  if (_shows('bhytTyLe') || _shows('donGia'))
                    Align(
                      alignment: Alignment.centerRight,
                      child: Text(
                        [
                          if (_shows('bhytTyLe')) 'Tỷ lệ thanh toán: ${drug.bhytTyLe}%',
                          if (_shows('donGia')) 'Đơn giá: ${formatVnd(drug.donGia)}',
                        ].join('\n'),
                        textAlign: TextAlign.right,
                        style: const TextStyle(fontStyle: FontStyle.italic),
                      ),
                    ),
                  const Align(
                    alignment: Alignment.centerRight,
                    child: Text(
                      '- Xem thêm -',
                      style: TextStyle(color: Color(0xFF007AFF)),
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
