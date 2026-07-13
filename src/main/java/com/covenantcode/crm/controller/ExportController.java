package com.covenantcode.crm.controller;

import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.service.ExportService;
import com.covenantcode.crm.service.LeadExportFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/export")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void exportLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            HttpServletResponse response) throws IOException {

        String filename = "leads_" + LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exportService.exportLeads(new LeadExportFilter(status, search, dateFrom, dateTo),
                response.getOutputStream());
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void exportStudents(
            @RequestParam(required = false) String search,
            HttpServletResponse response) throws IOException {

        String filename = "students_" + LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exportService.exportStudents(search, response.getOutputStream());
    }
}