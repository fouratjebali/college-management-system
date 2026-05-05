package MiniProjet_Backend.Backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPageResponseDTO {
    private List<AdminDashboardResponseDTO.UserRowDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private long totalUserElements;
    private long activeUserElements;
    private Map<String, Long> roleCounts;
    private List<String> departments;
    private List<String> groups;
    private List<DepartmentUserSummaryDTO> departmentSummaries;
    private List<DepartmentProfessorsDTO> professorsByDepartment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentUserSummaryDTO {
        private String department;
        private long students;
        private long professors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentProfessorsDTO {
        private String department;
        private List<AdminDashboardResponseDTO.UserRowDTO> professors;
    }
}
