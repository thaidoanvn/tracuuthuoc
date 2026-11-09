import { useEffect, useMemo, useState } from 'react'
import { api, getToken, setToken } from './api'

const EMPTY = {
  code: '',
  tenThuoc: '',
  hoatChat: '',
  dangBaoChe: '',
  hangSanXuat: '',
  hoiChuan: 'Không',
  kiemSoatDacBiet: 'Không',
  bhytTyLe: 100,
  donGia: 0,
  soGiayPhep: '',
  hamLuong: '',
  quyCach: '',
  donViTinh: '',
  congTy: '',
  nuocSanXuat: '',
  chiDinh: '',
  chongChiDinh: '',
  lieuDung: '',
  dieuKienBhyt: '',
  soQuyetDinh: '',
  nhom: '',
  nhomThuoc: '',
  bhyt: '',
  bhytThanhToan: '',
  nguoiThucHien: '',
  ngaySanXuat: '',
  hanDung: '',
  ngayHetHan: '',
  khongDung: '',
}

const FIELDS = [
  ['code', 'Mã thuốc', 'text'],
  ['tenThuoc', 'Tên thuốc', 'text'],
  ['soQuyetDinh', 'Số QĐ', 'text'],
  ['congTy', 'Tên công ty', 'text'],
  ['hoatChat', 'Hoạt chất', 'text'],
  ['hamLuong', 'Hàm lượng', 'text'],
  ['nhom', 'Nhóm', 'text'],
  ['nhomThuoc', 'Nhóm thuốc', 'text'],
  ['dangBaoChe', 'Dạng bào chế', 'text'],
  ['donViTinh', 'Đơn vị tính', 'text'],
  ['quyCach', 'Quy cách đóng gói', 'text'],
  ['soGiayPhep', 'SĐK', 'text'],
  ['hangSanXuat', 'Hãng sản xuất', 'text'],
  ['nuocSanXuat', 'Nước sản xuất', 'text'],
  ['donGia', 'Đơn giá (đồng)', 'number'],
  ['bhyt', 'BHYT', 'text'],
  ['bhytTyLe', 'Tỷ lệ', 'number'],
  ['bhytThanhToan', 'BHYT thanh toán', 'text'],
  ['kiemSoatDacBiet', 'KSĐB', 'text'],
  ['hoiChuan', 'Hội chuẩn', 'text'],
  ['nguoiThucHien', 'Người thực hiện', 'text'],
  ['ngaySanXuat', 'Ngày sản xuất', 'text'],
  ['hanDung', 'Hạn dùng', 'text', 'mm/dd/yyyy'],
  ['ngayHetHan', 'Ngày hết hạn', 'text', 'mm/dd/yyyy'],
  ['khongDung', 'Không dùng', 'text'],
]

const AREAS = [
  ['chiDinh', 'Chỉ định'],
  ['chongChiDinh', 'Chống chỉ định'],
  ['lieuDung', 'Liều dùng'],
  ['dieuKienBhyt', 'Điều kiện thanh toán'],
]

function toApiDate(value) {
  const text = String(value ?? '').trim()
  if (!text) {
    return null
  }
  const match = text.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})(?: (\d{1,2}):(\d{2})(?::(\d{2}))?)?$/)
  if (!match) {
    return undefined
  }
  const month = Number(match[1])
  const day = Number(match[2])
  const year = Number(match[3])
  const hour = match[4] == null ? 0 : Number(match[4])
  const minute = match[5] == null ? 0 : Number(match[5])
  const second = match[6] == null ? 0 : Number(match[6])
  if (hour > 23 || minute > 59 || second > 59) {
    return undefined
  }
  const date = new Date(Date.UTC(year, month - 1, day))
  if (date.getUTCFullYear() !== year || date.getUTCMonth() !== month - 1 || date.getUTCDate() !== day) {
    return undefined
  }
  const pad = (part) => String(part).padStart(2, '0')
  return `${year}-${pad(month)}-${pad(day)}T${pad(hour)}:${pad(minute)}:${pad(second)}`
}

function fromApiDate(value) {
  if (!value) {
    return ''
  }
  const match = String(value).match(/^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2}))?/)
  if (!match) {
    return ''
  }
  const [, year, month, day, hour = '00', minute = '00'] = match
  if (hour === '00' && minute === '00') {
    return `${month}/${day}/${year}`
  }
  return `${month}/${day}/${year} ${hour}:${minute}`
}

const PAGE_SIZE = 20

function money(value) {
  return new Intl.NumberFormat('vi-VN').format(value ?? 0)
}

export default function App() {
  const [token, setTokenState] = useState(getToken)
  const [username, setUsername] = useState('admin')
  const [password, setPassword] = useState('')
  const [drugs, setDrugs] = useState([])
  const [query, setQuery] = useState('')
  const [form, setForm] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [busy, setBusy] = useState(false)
  const [page, setPage] = useState(1)
  const [pageDraft, setPageDraft] = useState('1')

  async function load() {
    const data = await api('/api/admin/thuoc')
    setDrugs(data.result ?? [])
  }

  useEffect(() => {
    if (!token) {
      return
    }
    load().catch((err) => {
      if (err.status === 401) {
        setToken(null)
        setTokenState(null)
      }
      setError(err.message)
    })
  }, [token])

  const visible = useMemo(() => {
    const keyword = query.trim().toLowerCase()
    if (!keyword) {
      return drugs
    }
    return drugs.filter((drug) =>
      [drug.tenThuoc, drug.code, drug.hoatChat, drug.hangSanXuat]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(keyword)),
    )
  }, [drugs, query])

  const pageCount = Math.max(1, Math.ceil(visible.length / PAGE_SIZE))
  const currentPage = Math.min(page, pageCount)
  const pageStart = visible.length === 0 ? 0 : (currentPage - 1) * PAGE_SIZE
  const pageRows = visible.slice(pageStart, pageStart + PAGE_SIZE)

  useEffect(() => {
    setPage((current) => Math.min(current, pageCount))
  }, [pageCount])

  useEffect(() => {
    setPageDraft(String(currentPage))
  }, [currentPage])

  function jumpToPage(event) {
    event.preventDefault()
    const parsed = Number(pageDraft)
    if (!Number.isFinite(parsed)) {
      setPageDraft(String(currentPage))
      return
    }
    setPage(Math.min(pageCount, Math.max(1, Math.trunc(parsed))))
  }

  async function signIn(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const data = await api('/api/admin/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      })
      setToken(data.token)
      setTokenState(data.token)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  function signOut() {
    setToken(null)
    setTokenState(null)
    setDrugs([])
    setForm(null)
  }

  async function openEdit(id) {
    setError('')
    try {
      const drug = await api(`/api/admin/thuoc/${id}`)
      setForm({
        ...drug,
        hanDung: fromApiDate(drug.hanDung),
        ngayHetHan: fromApiDate(drug.ngayHetHan),
      })
    } catch (err) {
      setError(err.message)
    }
  }

  async function save(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setNotice('')
    const savedName = form.tenThuoc
    const updating = Boolean(form.id)
    const hanDung = toApiDate(form.hanDung)
    const ngayHetHan = toApiDate(form.ngayHetHan)
    if (hanDung === undefined || ngayHetHan === undefined) {
      setError('Hạn dùng và Ngày hết hạn nhập theo mm/dd/yyyy.')
      setBusy(false)
      return
    }
    const body = {
      ...form,
      bhytTyLe: Number(form.bhytTyLe),
      donGia: Number(form.donGia),
      hanDung,
      ngayHetHan,
    }
    try {
      if (form.id) {
        await api(`/api/admin/thuoc/${form.id}`, { method: 'PUT', body: JSON.stringify(body) })
      } else {
        await api('/api/admin/thuoc', { method: 'POST', body: JSON.stringify(body) })
      }
      setForm(null)
      setNotice(updating ? `Đã cập nhật ${savedName}.` : `Đã thêm ${savedName}.`)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function remove(drug) {
    if (!window.confirm(`Xóa ${drug.tenThuoc}?`)) {
      return
    }
    setError('')
    try {
      await api(`/api/admin/thuoc/${drug.id}`, { method: 'DELETE' })
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (!token) {
    return (
      <main className="login">
        <form className="card" onSubmit={signIn}>
          <p className="eyebrow">thaidoan</p>
          <h1>Quản trị tra cứu thuốc</h1>
          <label>
            Tên đăng nhập
            <input value={username} onChange={(event) => setUsername(event.target.value)} required />
          </label>
          <label>
            Mật khẩu
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
          </label>
          {error ? <p className="error">{error}</p> : null}
          <button type="submit" disabled={busy}>{busy ? 'Đang đăng nhập…' : 'Đăng nhập'}</button>
        </form>
      </main>
    )
  }

  return (
    <main className="app">
      <header>
        <div>
          <p className="eyebrow">thaidoan</p>
          <h1>Danh mục thuốc</h1>
        </div>
        <button type="button" className="ghost" onClick={signOut}>Đăng xuất</button>
      </header>
      <section className="toolbar">
        <input
          placeholder="Lọc theo tên, mã, hoạt chất, hãng"
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
            setPage(1)
          }}
        />
        <button type="button" onClick={() => { setError(''); setNotice(''); setForm({ ...EMPTY }) }}>Thêm thuốc</button>
        <label className="file">
          Nhập Excel
          <input
            type="file"
            accept=".xlsx,.xls"
            disabled={busy}
            onChange={async (event) => {
              const file = event.target.files?.[0]
              event.target.value = ''
              if (!file) {
                return
              }
              setBusy(true)
              setError('')
              setNotice('')
              try {
                const body = new FormData()
                body.append('file', file)
                const response = await fetch('/api/admin/thuoc/import', {
                  method: 'POST',
                  headers: { Authorization: `Bearer ${getToken()}` },
                  body,
                })
                const data = await response.json()
                if (!response.ok) {
                  if (response.status === 401) {
                    setToken(null)
                    setTokenState(null)
                  }
                  throw new Error(data.message || 'Không nhập được file')
                }
                const problems = (data.errors ?? []).map((item) => `Dòng ${item.row}: ${item.message}`).join('; ')
                setNotice(`Đã thêm ${data.created}, cập nhật ${data.updated}${data.failed ? `, ${data.failed} dòng lỗi` : ''}${problems ? `. ${problems}` : ''}`)
                await load()
              } catch (err) {
                setError(err.message)
              } finally {
                setBusy(false)
              }
            }}
          />
        </label>
      </section>
      {error ? <p className="error">{error}</p> : null}
      {notice ? <p className="notice">{notice}</p> : null}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th className="seq">Stt</th>
              <th>Mã</th>
              <th>Tên thuốc</th>
              <th>Hoạt chất</th>
              <th>Hãng</th>
              <th>BHYT</th>
              <th>Đơn giá</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {pageRows.length === 0 ? (
              <tr>
                <td colSpan={8}>Không có thuốc</td>
              </tr>
            ) : pageRows.map((drug, index) => (
              <tr key={drug.id}>
                <td className="seq">{pageStart + index + 1}</td>
                <td>{drug.code}</td>
                <td>
                  {drug.tenThuoc}
                  {drug.khongDung ? <span className="unused">Không dùng</span> : null}
                </td>
                <td>{drug.hoatChat}</td>
                <td>{drug.hangSanXuat}</td>
                <td>{drug.bhytTyLe}%</td>
                <td>{money(drug.donGia)}</td>
                <td className="actions">
                  <button type="button" className="ghost" onClick={() => openEdit(drug.id)}>Sửa</button>
                  <button type="button" className="danger" onClick={() => remove(drug)}>Xóa</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <nav className="pager">
        <span>
          {visible.length === 0
            ? '0 thuốc'
            : `${pageStart + 1}–${pageStart + pageRows.length} của ${visible.length}`}
        </span>
        <div>
          <button type="button" className="ghost" disabled={currentPage <= 1} onClick={() => setPage(currentPage - 1)}>Trước</button>
          <form noValidate onSubmit={jumpToPage}>
            <label>
              Trang
              <input
                className="page-jump"
                type="number"
                min={1}
                max={pageCount}
                value={pageDraft}
                onChange={(event) => setPageDraft(event.target.value)}
              />
            </label>
            <span>/ {pageCount}</span>
            <button type="submit" className="ghost">Đến</button>
          </form>
          <button type="button" className="ghost" disabled={currentPage >= pageCount} onClick={() => setPage(currentPage + 1)}>Sau</button>
        </div>
      </nav>
      {form ? (
        <form className="editor card" onSubmit={save}>
          <h2>{form.id ? 'Sửa thuốc' : 'Thêm thuốc'}</h2>
          <div className="grid">
            {FIELDS.map(([name, label, type, placeholder]) => (
              <label key={name}>
                {label}
                <input
                  type={type}
                  placeholder={placeholder}
                  value={form[name] ?? ''}
                  required={name === 'code' || name === 'tenThuoc'}
                  onChange={(event) => setForm({ ...form, [name]: event.target.value })}
                />
              </label>
            ))}
          </div>
          {AREAS.map(([name, label]) => (
            <label key={name}>
              {label}
              <textarea
                rows={3}
                value={form[name] ?? ''}
                onChange={(event) => setForm({ ...form, [name]: event.target.value })}
              />
            </label>
          ))}
          <div className="actions">
            <button type="submit" disabled={busy}>{busy ? 'Đang lưu…' : 'Lưu'}</button>
            <button type="button" className="ghost" onClick={() => setForm(null)}>Hủy</button>
          </div>
        </form>
      ) : null}
    </main>
  )
}
