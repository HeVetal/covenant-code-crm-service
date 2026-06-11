package com.covenantcode.crm.mapper;

import com.covenantcode.crm.dto.course.CourseCreateRequest;
import com.covenantcode.crm.dto.course.CourseResponse;
import com.covenantcode.crm.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CourseMapper {

    @Mapping(target = "createdAt", expression = "java(course.getCreatedAt() != null ? course.getCreatedAt().atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)")
    @Mapping(target = "updatedAt", expression = "java(course.getUpdatedAt() != null ? course.getUpdatedAt().atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)")
    CourseResponse toResponse(Course course);

    Course toEntity(CourseCreateRequest request);
}