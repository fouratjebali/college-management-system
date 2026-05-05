package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Integer> {
    Optional<AcademicYear> findByLabel(String label);

    Optional<AcademicYear> findByActiveTrue();

    boolean existsByLabel(String label);
}
