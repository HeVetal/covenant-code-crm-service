package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.service.LeadExportFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Disabled
@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private ExportServiceImpl exportService;

    private Lead testLead;
    private LeadExportFilter testFilter;

    @BeforeEach
    void setUp() {
        User manager = User.builder()
                .firstName("Анна")
                .lastName("Иванова")
                .build();

        Course course = Course.builder()
                .id(1L)
                .title("Java Backend Developer")
                .build();

        testLead = Lead.builder()
                .id(100L)
                .firstName("Иван")
                .lastName("Петров")
                .phone("+79001234567")
                .email("ivan@example.com")
                .status(LeadStatus.IN_PROGRESS)
                .interestedCourse(course)
                .assignedManager(manager)
                .createdAt(OffsetDateTime.of(2026, 7, 13, 10, 30, 0, 0, ZoneOffset.UTC))
                .build();

        testFilter = new LeadExportFilter(
                LeadStatus.IN_PROGRESS,
                "ivan",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31)
        );
    }

    @Test
    @DisplayName("Тест 1: экспорт лидов — CSV содержит заголовок и строку данных")
    void exportLeads_shouldContainHeaderAndDataRow() throws IOException {
        when(leadRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(testLead));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        exportService.exportLeads(testFilter, outputStream);

        String csvContent = outputStream.toString("UTF-8");

        byte[] bytes = outputStream.toByteArray();
        assertThat(bytes).startsWith(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        assertThat(csvContent).contains("ID,Имя,Фамилия,Телефон,Email,Статус,Курс,Менеджер,Дата создания");

        assertThat(csvContent).contains(
                "100",
                "Иван",
                "Петров",
                "+79001234567",
                "ivan@example.com",
                "IN_PROGRESS",
                "Java Backend Developer",
                "Анна Иванова",
                "13.07.2026"
        );

        String[] lines = csvContent.split("\n");
        assertThat(lines).hasSize(2);
    }

    @Test
    @DisplayName("Тест 2: экспорт пустого списка — только заголовок")
    void exportLeads_withEmptyList_shouldContainOnlyHeader() throws IOException {
        when(leadRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        exportService.exportLeads(testFilter, outputStream);

        String csvContent = outputStream.toString("UTF-8");

        byte[] bytes = outputStream.toByteArray();
        assertThat(bytes).startsWith(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        String[] lines = csvContent.split("\n");
        assertThat(lines).hasSize(1);
        assertThat(lines[0]).contains("ID,Имя,Фамилия,Телефон,Email,Статус,Курс,Менеджер,Дата создания");

        assertThat(csvContent).doesNotContain("\n100");
        assertThat(csvContent).doesNotContain("\nИван");
    }
}