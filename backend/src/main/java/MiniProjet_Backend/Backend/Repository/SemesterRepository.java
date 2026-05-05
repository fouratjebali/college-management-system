package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Integer> {
    List<Semester> findByAcademicYearIdOrderByStartDateAsc(Integer academicYearId);

    Optional<Semester> findByActiveTrue();

    boolean existsByAcademicYearIdAndCode(Integer academicYearId, String code);
}
