package com.aiqa.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/** Creates downloadable artifacts from a completed Auravis pipeline result. */
@Service
public class PipelineExportService {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] toExcel(String resultJson) {
        try {
            PipelineModels.PipelineResult result = mapper.readValue(resultJson, PipelineModels.PipelineResult.class);
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Generated Test Cases");
                String[] headers = {
                        "Requirement", "Test Case ID", "Scenario", "Type", "Priority", "Preconditions",
                        "Steps", "Test Data", "Expected Result", "Automation Candidate", "Execution Status"
                };

                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                headerStyle.setWrapText(true);

                CellStyle wrapStyle = workbook.createCellStyle();
                wrapStyle.setWrapText(true);
                wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);

                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowIndex = 1;
                for (PipelineModels.RequirementResult requirement : result.requirements()) {
                    for (PipelineModels.TestCaseResult test : requirement.testCases()) {
                        Row row = sheet.createRow(rowIndex++);
                        String executionStatus = test.execution() == null ? "NOT_EXECUTED" : test.execution().status();
                        String[] values = {
                                requirement.title(), test.id(), test.title(), test.type(), test.priority(),
                                test.preconditions(), join(test.steps()), test.testData(), test.expectedResult(),
                                test.automationCandidate(), executionStatus
                        };
                        for (int i = 0; i < values.length; i++) {
                            Cell cell = row.createCell(i);
                            cell.setCellValue(values[i] == null ? "" : values[i]);
                            cell.setCellStyle(wrapStyle);
                        }
                    }
                }

                int[] widths = {28, 14, 34, 18, 12, 34, 55, 36, 50, 20, 18};
                for (int i = 0; i < widths.length; i++) {
                    sheet.setColumnWidth(i, widths[i] * 256);
                }
                sheet.createFreezePane(0, 1);
                workbook.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create Excel export", e);
        }
    }

    private String join(List<String> values) {
        return values == null ? "" : String.join("\n", values);
    }
}
