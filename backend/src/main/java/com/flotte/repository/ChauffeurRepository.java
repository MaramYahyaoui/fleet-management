package com.flotte.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.flotte.entity.Chauffeur;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChauffeurRepository extends JpaRepository<Chauffeur, Long> {
	Optional<Chauffeur> findByPermis(String permis);

    boolean existsByPermis(String permis);

    List<Chauffeur> findByExperienceGreaterThanEqual(Integer experienceMin);

    // Chauffeurs disponibles (sans mission en cours)
    @Query("""
        SELECT c FROM Chauffeur c
        WHERE c.id NOT IN (
            SELECT t.chauffeur.id FROM TrajetMission t
            WHERE t.statut = 'EN_COURS'
        )
        """)
    List<Chauffeur> findChauffeursdisponibles();

    // Chauffeurs les plus actifs
    @Query("SELECT c FROM Chauffeur c LEFT JOIN c.trajets t GROUP BY c ORDER BY COUNT(t) DESC")
    List<Chauffeur> findChauffeursLesPlasActifs();

    List<Chauffeur> findByNomContainingIgnoreCase(String nom);

}
