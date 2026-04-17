package MiniProjet_Backend.Backend.Repository;

import MiniProjet_Backend.Backend.Model.Annonce;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Integer> {
    List<Annonce> findByAdministrateurId(Integer administrateurId);
    List<Annonce> findByCibleGlobaleTrue();

    Page<Annonce> findByAdministrateurId(Integer administrateurId, Pageable pageable);

    Page<Annonce> findByCibleGlobaleTrue(Pageable pageable);

    @Query("""
            select a from Annonce a
            where a.datePublication <= :now
              and a.dateExpiration >= :now
            order by a.datePublication desc
            """)
    Page<Annonce> findActive(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
            select a from Annonce a
            where (:activeOnly = false or (a.datePublication <= :now and a.dateExpiration >= :now))
              and (
                  a.cibleGlobale = true
                  or (:role is not null and upper(a.cibleRole) = upper(:role))
              )
            order by a.datePublication desc
            """)
    Page<Annonce> findVisibleForRole(
            @Param("role") String role,
            @Param("now") LocalDateTime now,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );
}

