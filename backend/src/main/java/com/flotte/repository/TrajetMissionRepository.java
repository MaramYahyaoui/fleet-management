package com.flotte.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flotte.entity.TrajetMission;

@Repository
public interface TrajetMissionRepository extends JpaRepository<TrajetMission, Long> {

	 List<TrajetMission> findByVehiculeId(Long vehiculeId);

	    List<TrajetMission> findByChauffeurId(Long chauffeurId);

	    List<TrajetMission> findByStatut(String statut);

	    List<TrajetMission> findByDateMissionBetween(LocalDate debut, LocalDate fin);

	    // Missions en cours
	    List<TrajetMission> findByStatutIn(List<String> statuts);

	    // Nombre de missions par véhicule
	    @Query("SELECT t.vehicule.id, COUNT(t) FROM TrajetMission t GROUP BY t.vehicule.id")
	    List<Object[]> countMissionsParVehicule();

	    // Distance totale par véhicule
	    @Query("SELECT t.vehicule.id, SUM(t.distance) FROM TrajetMission t GROUP BY t.vehicule.id")
	    List<Object[]> sumDistanceParVehicule();

	    // Missions d'un véhicule sur une période
	    @Query("""
	        SELECT t FROM TrajetMission t
	        WHERE t.vehicule.id = :vehiculeId
	        AND t.dateMission BETWEEN :debut AND :fin
	        """)
	    List<TrajetMission> findByVehiculeAndPeriode(
	            @Param("vehiculeId") Long vehiculeId,
	            @Param("debut") LocalDate debut,
	            @Param("fin") LocalDate fin
	    );

	    // Vérifier si chauffeur a déjà une mission en cours
	    @Query("""
	        SELECT COUNT(t) > 0 FROM TrajetMission t
	        WHERE t.chauffeur.id = :chauffeurId
	        AND t.statut = 'EN_COURS'
	        """)
	    boolean chauffeurAMissionEnCours(@Param("chauffeurId") Long chauffeurId);

	    // Vérifier si véhicule est déjà en mission
	    @Query("""
	        SELECT COUNT(t) > 0 FROM TrajetMission t
	        WHERE t.vehicule.id = :vehiculeId
	        AND t.statut = 'EN_COURS'
	        """)
	    boolean vehiculeEnMission(@Param("vehiculeId") Long vehiculeId);
}
