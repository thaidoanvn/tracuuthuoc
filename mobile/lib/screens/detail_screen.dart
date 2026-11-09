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

class _DetailScreenState extends State<DetailScreen> {
  late Future<ThuocDetail> _detail;

  @override
  void initState() {
    super.initState();
    _detail = widget.api.detail(widget.id);
  }

  void _reload() {
    setState(() {
      _detail = widget.api.detail(widget.id);
    });
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<ThuocDetail>(
      future: _detail,
      builder: (context, snapshot) {
        final title = snapshot.data?.tenThuoc ?? 'Thông tin thuốc';
        return Scaffold(
          appBar: AppBar(title: Text(title)),
          body: _body(snapshot),
        );
      },
    );
  }

  Widget _body(AsyncSnapshot<ThuocDetail> snapshot) {
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
    final drug = snapshot.data!;
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
      children: [
        Text(
          drug.tenThuoc,
          style: Theme.of(context).textTheme.headlineSmall,
        ),
        const SizedBox(height: 4),
        Text('Mã: ${drug.code}', style: Theme.of(context).textTheme.titleMedium),
        const SizedBox(height: 16),
        if (drug.soQuyetDinh.isNotEmpty) _row('Số QĐ', drug.soQuyetDinh),
        _row('Giấy phép', drug.soGiayPhep),
        _row('Hoạt chất', drug.hoatChat),
        _row('Hàm lượng', drug.hamLuong),
        _row('Dạng bào chế', drug.dangBaoChe),
        _row('Quy cách đóng gói', drug.quyCach),
        _row('Đơn vị tính', drug.donViTinh),
        _row('Đơn giá', '${formatVnd(drug.donGia)} / ${drug.donViTinh}'),
        _row('Công ty', drug.congTy),
        _row('Hãng sản xuất', drug.hangSanXuat),
        _row('Nước sản xuất', drug.nuocSanXuat),
        _row('Hội chẩn', displayOrKhong(drug.hoiChuan)),
        _row('Kiểm soát đặc biệt', displayOrKhong(drug.kiemSoatDacBiet)),
        if (drug.nhom.isNotEmpty) _row('Nhóm', drug.nhom),
        if (drug.nhomThuoc.isNotEmpty) _row('Nhóm thuốc', drug.nhomThuoc),
        if (drug.bhyt.isNotEmpty) _row('BHYT', drug.bhyt),
        _row('Tỷ lệ thanh toán BHYT', '${drug.bhytTyLe}%'),
        if (drug.bhytThanhToan.isNotEmpty) _row('BHYT thanh toán', drug.bhytThanhToan),
        _row('Điều kiện thanh toán BHYT', drug.dieuKienBhyt),
        if (drug.nguoiThucHien.isNotEmpty) _row('Người thực hiện', drug.nguoiThucHien),
        if (drug.ngaySanXuat.isNotEmpty) _row('Ngày sản xuất', drug.ngaySanXuat),
        if (drug.hanDung.isNotEmpty) _row('Hạn dùng', formatDateTime(drug.hanDung)),
        if (drug.ngayHetHan.isNotEmpty) _row('Ngày hết hạn', formatDateTime(drug.ngayHetHan)),
        if (drug.khongDung.isNotEmpty) _row('Không dùng', drug.khongDung),
        _row('Chỉ định', drug.chiDinh),
        _row('Chống chỉ định', drug.chongChiDinh),
        _row('Liều dùng', drug.lieuDung),
        const SizedBox(height: 8),
        const Text(
          'Giá và tỷ lệ BHYT do thaidoan công bố và có thể thay đổi.',
          style: TextStyle(fontStyle: FontStyle.italic, color: Color(0xFF546E7A)),
        ),
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
