class ThuocSummary {
  const ThuocSummary({
    required this.id,
    required this.code,
    required this.tenThuoc,
    required this.hoatChat,
    required this.dangBaoChe,
    required this.hangSanXuat,
    required this.hoiChuan,
    required this.kiemSoatDacBiet,
    required this.bhytTyLe,
    required this.donGia,
  });

  final int id;
  final String code;
  final String tenThuoc;
  final String hoatChat;
  final String dangBaoChe;
  final String hangSanXuat;
  final String hoiChuan;
  final String kiemSoatDacBiet;
  final int bhytTyLe;
  final num donGia;

  factory ThuocSummary.fromJson(Map<String, dynamic> json) {
    return ThuocSummary(
      id: json['id'] as int,
      code: json['code'] as String? ?? '',
      tenThuoc: json['tenThuoc'] as String? ?? '',
      hoatChat: json['hoatChat'] as String? ?? '',
      dangBaoChe: json['dangBaoChe'] as String? ?? '',
      hangSanXuat: json['hangSanXuat'] as String? ?? '',
      hoiChuan: json['hoiChuan'] as String? ?? '',
      kiemSoatDacBiet: json['kiemSoatDacBiet'] as String? ?? '',
      bhytTyLe: (json['bhytTyLe'] as num?)?.toInt() ?? 0,
      donGia: json['donGia'] as num? ?? 0,
    );
  }
}

class ThuocDetail extends ThuocSummary {
  const ThuocDetail({
    required super.id,
    required super.code,
    required super.tenThuoc,
    required super.hoatChat,
    required super.dangBaoChe,
    required super.hangSanXuat,
    required super.hoiChuan,
    required super.kiemSoatDacBiet,
    required super.bhytTyLe,
    required super.donGia,
    required this.soGiayPhep,
    required this.hamLuong,
    required this.quyCach,
    required this.donViTinh,
    required this.congTy,
    required this.nuocSanXuat,
    required this.chiDinh,
    required this.chongChiDinh,
    required this.lieuDung,
    required this.dieuKienBhyt,
    required this.soQuyetDinh,
    required this.nhom,
    required this.nhomThuoc,
    required this.bhyt,
    required this.bhytThanhToan,
    required this.nguoiThucHien,
    required this.ngaySanXuat,
    required this.hanDung,
    required this.ngayHetHan,
    required this.khongDung,
  });

  final String soGiayPhep;
  final String hamLuong;
  final String quyCach;
  final String donViTinh;
  final String congTy;
  final String nuocSanXuat;
  final String chiDinh;
  final String chongChiDinh;
  final String lieuDung;
  final String dieuKienBhyt;
  final String soQuyetDinh;
  final String nhom;
  final String nhomThuoc;
  final String bhyt;
  final String bhytThanhToan;
  final String nguoiThucHien;
  final String ngaySanXuat;
  final String hanDung;
  final String ngayHetHan;
  final String khongDung;

  factory ThuocDetail.fromJson(Map<String, dynamic> json) {
    final summary = ThuocSummary.fromJson(json);
    return ThuocDetail(
      id: summary.id,
      code: summary.code,
      tenThuoc: summary.tenThuoc,
      hoatChat: summary.hoatChat,
      dangBaoChe: summary.dangBaoChe,
      hangSanXuat: summary.hangSanXuat,
      hoiChuan: summary.hoiChuan,
      kiemSoatDacBiet: summary.kiemSoatDacBiet,
      bhytTyLe: summary.bhytTyLe,
      donGia: summary.donGia,
      soGiayPhep: json['soGiayPhep'] as String? ?? '',
      hamLuong: json['hamLuong'] as String? ?? '',
      quyCach: json['quyCach'] as String? ?? '',
      donViTinh: json['donViTinh'] as String? ?? '',
      congTy: json['congTy'] as String? ?? '',
      nuocSanXuat: json['nuocSanXuat'] as String? ?? '',
      chiDinh: json['chiDinh'] as String? ?? '',
      chongChiDinh: json['chongChiDinh'] as String? ?? '',
      lieuDung: json['lieuDung'] as String? ?? '',
      dieuKienBhyt: json['dieuKienBhyt'] as String? ?? '',
      soQuyetDinh: json['soQuyetDinh'] as String? ?? '',
      nhom: json['nhom'] as String? ?? '',
      nhomThuoc: json['nhomThuoc'] as String? ?? '',
      bhyt: json['bhyt'] as String? ?? '',
      bhytThanhToan: json['bhytThanhToan'] as String? ?? '',
      nguoiThucHien: json['nguoiThucHien'] as String? ?? '',
      ngaySanXuat: json['ngaySanXuat'] as String? ?? '',
      hanDung: json['hanDung'] as String? ?? '',
      ngayHetHan: json['ngayHetHan'] as String? ?? '',
      khongDung: json['khongDung'] as String? ?? '',
    );
  }
}

String formatVnd(num value) {
  final digits = value.round().toString();
  final buffer = StringBuffer();
  for (var i = 0; i < digits.length; i++) {
    if (i > 0 && (digits.length - i) % 3 == 0) {
      buffer.write('.');
    }
    buffer.write(digits[i]);
  }
  return '$buffer đ';
}

String formatDateTime(String value) {
  final parsed = DateTime.tryParse(value);
  if (parsed == null) {
    return value;
  }
  String two(int number) => number.toString().padLeft(2, '0');
  return '${two(parsed.day)}/${two(parsed.month)}/${parsed.year} ${two(parsed.hour)}:${two(parsed.minute)}';
}

String displayOrKhong(String value) {
  final trimmed = value.trim();
  if (trimmed.isEmpty) {
    return 'Không';
  }
  return trimmed;
}
