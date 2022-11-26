package quru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;
import static org.assertj.core.api.Assertions.assertThat;



public class FileParseTest {

    ClassLoader cl = FileParseTest.class.getClassLoader();

    @Test
    void zipTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("files.zip");
             ZipInputStream zis = new ZipInputStream(is);
             ZipFile zipFile = new java.util.zip.ZipFile("src/test/resources/files.zip")) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName.contains(".pdf")) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        PDF pdf = new PDF(inputStream);
                        String actualValue = pdf.text;
                        assertThat(actualValue).contains("TEST123");
                    }
                } else if (entryName.contains(".csv")) {
                    try (CSVReader reader = new CSVReader(new InputStreamReader(zipFile.getInputStream(entry)))) {
                        List<String[]> content = reader.readAll();
                        String[] row = content.get(1);
                        assertThat(row[0]).isEqualTo("Дата и время выгрузки;27 сентября 2022 10:56:02");
                    }
                }
                else {
                    XLS xls = new XLS(zipFile.getInputStream(entry));
                    assertThat(
                            xls.excel.getSheetAt(0)
                                    .getRow(1)
                                    .getCell(1)
                                    .getStringCellValue()
                    ).contains("27 сентября 2022 10:56:02");
                }
            }
        }
    }

    @Test
    void jsonTest() throws IOException {
        File file = new File("src/test/resources/json1.json");
        ObjectMapper objectMapper = new ObjectMapper();
        Services service  = objectMapper.readValue(file, Services.class);

        assertThat(service.getName()).isEqualTo("mts");
        assertThat(service.getDescription()).isEqualTo("mts bills");
        assertThat(service.getIsActive()).isEqualTo(true);
    }

    @Test
    void jsonTest2() throws IOException {
        File file = new File("src/test/resources/json2.json");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Services> servicesList  = objectMapper.readValue(file, new TypeReference<>(){});

        assertThat(servicesList).hasSize(2);
        assertThat(servicesList.get(0).getName()).isEqualTo("mts");
        assertThat(servicesList.get(0).getDescription()).isEqualTo("mts bills");
        assertThat(servicesList.get(0).getIsActive()).isEqualTo(true);
    }
}
