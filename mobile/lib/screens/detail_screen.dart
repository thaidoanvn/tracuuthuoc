import 'package:flutter/material.dart';

import '../api/thuoc_api.dart';
import '../models/thuoc.dart';

class DetailScreen extends StatefulWidget {
  const DetailScreen({super.key, required this.api, required this.id});

  final ThuocApi api;
  final int id;

  @override
  State<DetailScreen> createState() => _DetailScreenState();
}

class _DrugView {
  const _DrugView(this.drug, this.display);

  final ThuocDetail drug;
  final Map<String, bool> display;
}

class _DetailScreenState extends State<DetailScreen> {
  late Future<_DrugView> _detail;

  @override
  void initState() {
    super.initState();
    _detail = _load();
  }

  Future<_DrugView> _load() async {
    final drug = await widget.api.detail(widget.id);
    Map<String, bool> display = {};
    try {
      display = await widget.api.displayFields();
    } catch (_) {
      // Show every field when the setting cannot be loaded.
    }
    return _DrugView(drug, display);
  }

  void _reload() {
    setState(() {
      _detail = _load();
    });
  }

  bool _shows(Map<String, bool> display, String key) => display[key] != false;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<_DrugView>(
      future: _detail,
      builder: (context, snapshot) {
        final title = snapshot.data?.drug.tenThuoc ?? 'Thông tin thuốc';
        return Scaffold(
          appBar: AppBar(title: Text(title)),
          body: _body(snapshot),
        );
      },
    );
  }

  Widget _body(AsyncSnapshot<_DrugView> snapshot) {
    if (snapshot.connectionState != ConnectionState.done) {
      return const Center(child: CircularProgressIndicator());
    }
    if (snapshot.hasError) {
      final message = snapshot.error is ThuocApiException
          ? snapshot.error.toString()
          : 'Không kết nối được máy chủ. Kiểm tra API đang chạy.';
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.cloud_off, size: 48, color: Color(0xFF123A8A)),
              const SizedBox(height: 12),
              Text(message, textAlign: TextAlign.center),
              const SizedBox(height: 16),
              FilledButton(onPressed: _reload, child: const Text('Thử lại')),
            ],
          ),
        ),
      );
    }
    final drug = snapshot.data!.drug;
    final display = snapshot.data!.display;
    final showPriceNote = _shows(display, 'donGia') || _shows(display, 'bhytTyLe');
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
      children: [
        Text(
          drug.tenThuoc,
          style: Theme.of(context).textTheme.headlineSmall,
        ),
        if (_shows(display, 'code')) ...[
          const SizedBox(height: 4),
          Text('Mã: ${drug.code}', style: Theme.of(context).textTheme.titleMedium),
        ],
        const SizedBox(height: 16),
        if (_shows(display, 'soQuyetDinh') && drug.soQuyetDinh.isNotEmpty) _row('Số QĐ', drug.soQuyetDinh),
        if (_shows(display, 'soGiayPhep')) _row('Giấy phép', drug.soGiayPhep),
        if (_shows(display, 'hoatChat')) _row('Hoạt chất', drug.hoatChat),
        if (_shows(display, 'hamLuong')) _row('Hàm lượng', drug.hamLuong),
        if (_shows(display, 'dangBaoChe')) _row('Dạng bào chế', drug.dangBaoChe),
        if (_shows(display, 'quyCach')) _row('Quy cách đóng gói', drug.quyCach),
        if (_shows(display, 'donViTinh')) _row('Đơn vị tính', drug.donViTinh),
        if (_shows(display, 'donGia')) _row('Đơn giá', '${formatVnd(drug.donGia)} / ${drug.donViTinh}'),
        if (_shows(display, 'congTy')) _row('Công ty', drug.congTy),
        if (_shows(display, 'hangSanXuat')) _row('Hãng sản xuất', drug.hangSanXuat),
        if (_shows(display, 'nuocSanXuat')) _row('Nước sản xuất', drug.nuocSanXuat),
        if (_shows(display, 'hoiChuan')) _row('Hội chẩn', displayOrKhong(drug.hoiChuan)),
        if (_shows(display, 'kiemSoatDacBiet')) _row('Kiểm soát đặc biệt', displayOrKhong(drug.kiemSoatDacBiet)),
        if (_shows(display, 'nhom') && drug.nhom.isNotEmpty) _row('Nhóm', drug.nhom),
        if (_shows(display, 'nhomThuoc') && drug.nhomThuoc.isNotEmpty) _row('Nhóm thuốc', drug.nhomThuoc),
        if (_shows(display, 'bhyt') && drug.bhyt.isNotEmpty) _row('BHYT', drug.bhyt),
        if (_shows(display, 'bhytTyLe')) _row('Tỷ lệ thanh toán BHYT', '${drug.bhytTyLe}%'),
        if (_shows(display, 'bhytThanhToan') && drug.bhytThanhToan.isNotEmpty)
          _row('BHYT thanh toán', drug.bhytThanhToan),
        if (_shows(display, 'dieuKienBhyt')) _row('Điều kiện thanh toán BHYT', drug.dieuKienBhyt),
        if (_shows(display, 'nguoiThucHien') && drug.nguoiThucHien.isNotEmpty)
          _row('Người thực hiện', drug.nguoiThucHien),
        if (_shows(display, 'ngaySanXuat') && drug.ngaySanXuat.isNotEmpty) _row('Ngày sản xuất', drug.ngaySanXuat),
        if (_shows(display, 'hanDung') && drug.hanDung.isNotEmpty) _row('Hạn dùng', formatDateTime(drug.hanDung)),
        if (_shows(display, 'ngayHetHan') && drug.ngayHetHan.isNotEmpty)
          _row('Ngày hết hạn', formatDateTime(drug.ngayHetHan)),
        if (_shows(display, 'khongDung') && drug.khongDung.isNotEmpty) _row('Không dùng', drug.khongDung),
        if (_shows(display, 'chiDinh')) _row('Chỉ định', drug.chiDinh),
        if (_shows(display, 'chongChiDinh')) _row('Chống chỉ định', drug.chongChiDinh),
        if (_shows(display, 'lieuDung')) _row('Liều dùng', drug.lieuDung),
        if (showPriceNote) ...[
          const SizedBox(height: 8),
          const Text(
            'Giá và tỷ lệ BHYT do thaidoan công bố và có thể thay đổi.',
            style: TextStyle(fontStyle: FontStyle.italic, color: Color(0xFF546E7A)),
          ),
        ],
      ],
    );
  }

  Widget _row(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              color: Color(0xFF123A8A),
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 2),
          Text(value.isEmpty ? '—' : value),
        ],
      ),
    );
  }
}
