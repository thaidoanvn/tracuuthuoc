package com.bvnd115.tracuuthuoc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bvnd115.tracuuthuoc.security.ApiKeyFilter;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ThuocControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request.header(ApiKeyFilter.HEADER, "test-key"));
    }

    @Test
    void missingApiKeyIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/thuoc/suggest").param("keyword", "par").param("tenThuoc", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Khóa xác thực không hợp lệ"));
    }

    @Test
    void searchByNameReturnsSummaries() throws Exception {
        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"para","tenThuoc":true,"batDauVoi":false,"hoatChat":false,"cssx":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.result[0].code").exists())
                .andExpect(jsonPath("$.result[0].chiDinh").doesNotExist());
    }

    @Test
    void startsWithExcludesNamesThatOnlyContainTheKeyword() throws Exception {
        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"para","tenThuoc":false,"batDauVoi":true,"hoatChat":false,"cssx":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[*].tenThuoc", everyItem(startsWith("Para"))))
                .andExpect(jsonPath("$.result[?(@.code == 'HO-PARA')]", empty()));
    }

    @Test
    void activeIngredientFindsNamesThatDoNotContainTheKeyword() throws Exception {
        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"para","tenThuoc":false,"batDauVoi":false,"hoatChat":true,"cssx":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[?(@.code == 'EFFE500')]", not(empty())));
    }

    @Test
    void manufacturerFilterMatchesHangSanXuat() throws Exception {
        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"stada","tenThuoc":false,"batDauVoi":false,"hoatChat":false,"cssx":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(2)));
    }

    @Test
    void blankKeywordReturnsNoRows() throws Exception {
        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"  ","tenThuoc":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", empty()));
    }

    @Test
    void missingKeywordIsBadRequest() throws Exception {
        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tenThuoc":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Thiếu từ khóa"));
    }

    @Test
    void suggestStartsAfterThreeCharacters() throws Exception {
        perform(get("/api/thuoc/suggest").param("keyword", "pa").param("tenThuoc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", empty()));

        perform(get("/api/thuoc/suggest").param("keyword", "par").param("tenThuoc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.result[0].tenThuoc").exists())
                .andExpect(jsonPath("$.result[0].chiDinh").doesNotExist());
    }

    @Test
    void detailAddsClinicalFieldsAndUnknownIdIsNotFound() throws Exception {
        MvcResult search = perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"para","tenThuoc":true}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String body = search.getResponse().getContentAsString();
        String id = body.replaceAll("(?s).*\"id\"\\s*:\\s*(\\d+).*", "$1");

        perform(get("/api/thuoc/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chiDinh").isNotEmpty())
                .andExpect(jsonPath("$.soGiayPhep").isNotEmpty())
                .andExpect(jsonPath("$.dieuKienBhyt").isNotEmpty());

        perform(get("/api/thuoc/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Không tìm thấy thuốc"));
    }

    @Test
    void adminLoginProtectsCatalogChanges() throws Exception {
        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"wrong","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());

        MvcResult login = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String token = login.getResponse().getContentAsString().replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/admin/thuoc"))
                .andExpect(status().isUnauthorized());

        MvcResult created = mockMvc.perform(post("/api/admin/thuoc")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ADM1","tenThuoc":"Thuốc quản trị","hoatChat":"Test","bhytTyLe":80,"donGia":1500}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ADM1"))
                .andExpect(jsonPath("$.tenThuoc").value("Thuốc quản trị"))
                .andReturn();
        String id = created.getResponse().getContentAsString().replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");

        mockMvc.perform(post("/api/admin/thuoc")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ADM1","tenThuoc":"Thuốc trùng mã","hoatChat":"Test","bhytTyLe":80,"donGia":1500}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Mã thuốc đã tồn tại"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/admin/thuoc/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"PARA500","tenThuoc":"Thuốc quản trị","hoatChat":"Test","bhytTyLe":80,"donGia":1500}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/admin/thuoc/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void excelImportCreatesUpdatesAndHidesUnusedDrugs() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String token = login.getResponse().getContentAsString().replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(multipart("/api/admin/thuoc/import")
                        .file(workbook("Thuốc từ Excel", ""))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(2))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.errors[0].message").value("Thiếu tên thuốc"));

        mockMvc.perform(multipart("/api/admin/thuoc/import")
                        .file(workbook("Thuốc từ Excel đã sửa", ""))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created").value(0))
                .andExpect(jsonPath("$.updated").value(2));

        MvcResult found = perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"Thuốc từ Excel đã sửa","tenThuoc":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andExpect(jsonPath("$.result[0].code").value("XLS1"))
                .andExpect(jsonPath("$.result[0].donGia").value(15000))
                .andExpect(jsonPath("$.result[0].bhytTyLe").value(80))
                .andReturn();
        String id = found.getResponse().getContentAsString().replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1");
        perform(get("/api/thuoc/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hanDung").value("2026-12-24T08:30:00"))
                .andExpect(jsonPath("$.ngayHetHan").value("2028-01-01T00:00:00"));

        perform(post("/api/thuoc/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"Thuốc không dùng","tenThuoc":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", empty()));
    }

    private MockMultipartFile workbook(String name, String unused) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            String[] titles = {
                    "Stt", "Mã thuốc", "Số QĐ", "Tên công ty,", "Hoạt chất", "Tên thuốc", "Hàm lượng", "Nhóm",
                    "Dạng bào chế", "Đơn vị tính", "Quy cách đóng gói", "SĐK", "Hãng sản xuất", "Nước sản xuất",
                    "Đơn giá", "Chỉ định", "Chống chỉ định", "Liều dùng", "Điều kiện thanh toán", "BHYT", "Tỷ lệ",
                    "KSĐB", "Hội chuẩn", "Người thực hiện", "Ngày sản xuất", "Hạn dùng", "Ngày hết hạn",
                    "BHYT thanh toán", "Nhóm thuốc", "Không dùng"
            };
            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }
            Row first = sheet.createRow(1);
            first.createCell(1).setCellValue("XLS1");
            first.createCell(2).setCellValue("123/QĐ");
            first.createCell(5).setCellValue(name);
            first.createCell(14).setCellValue(15000);
            first.createCell(20).setCellValue(80);
            first.createCell(25).setCellValue("24/12/2026 08:30");
            first.createCell(26).setCellValue("01/01/2028");
            first.createCell(29).setCellValue(unused);
            Row missingName = sheet.createRow(2);
            missingName.createCell(1).setCellValue("XLS-MISSING");
            Row hidden = sheet.createRow(3);
            hidden.createCell(1).setCellValue("XLS2");
            hidden.createCell(5).setCellValue("Thuốc không dùng");
            hidden.createCell(29).setCellValue("x");
            workbook.write(output);
            return new MockMultipartFile(
                    "file",
                    "thuoc.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    output.toByteArray()
            );
        }
    }
}
