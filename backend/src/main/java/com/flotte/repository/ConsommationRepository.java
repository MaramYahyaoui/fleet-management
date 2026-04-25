package com.flotte.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flotte.entity.Consommation;

@Repository
public interface ConsommationRepository extends JpaRepository<Consommation, Long> {

	 List<Consommation> findByVehiculeId(Long vehiculeId);

	    List<Consommation> findByDateBetween(LocalDate debut, LocalDate fin);

	    List<Consommation> findByVehiculeIdAndDateBetween(Long vehiculeId, LocalDate debut, LocalDate fin);

	    // Coût total par véhicule
	    @Query("SELECT c.vehicule.id, SUM(c.coutTotal) FROM Consommation c GROUP BY c.vehicule.id ORDER BY SUM(c.coutTotal) DESC")
	    List<Object[]> sumCoutParVehicule();

	    // Coût total par véhicule pour un mois donné
	    @Query("""
	        SELECT c.vehicule.id, SUM(c.coutTotal), SUM(c.quantiteCarburant)
	        FROM Consommation c
	        WHERE YEAR(c.date) = :annee AND MONTH(c.date) = :mois
	        GROUP BY c.vehicule.id
	        """)
	    List<Object[]> sumCoutParVehiculeEtMois(
	            @Param("annee") int annee,
	            @Param("mois") int mois
	    );

	    // Consommation mensuelle d'un véhicule
	    @Query("""
	        SELECT YEAR(c.date), MONTH(c.date), SUM(c.coutTotal), SUM(c.quantiteCarburant)
	        FROM Consommation c
	        WHERE c.vehicule.id = :vehiculeId
	        GROUP BY YEAR(c.date), MONTH(c.date)
	        ORDER BY YEAR(c.date), MONTH(c.date)
	        """)
	    List<Object[]> consommationMensuelleParVehicule(@Param("vehiculeId") Long vehiculeId);

	    // Coût total global
	    @Query("SELECT SUM(c.coutTotal) FROM Consommation c")
	    Double sumCoutTotal();

	    // Coût moyen par plein
	    @Query("SELECT AVG(c.coutTotal) FROM Consommation c WHERE c.vehicule.id = :vehiculeId")
	    Double avgCoutParPlein(@Param("vehiculeId") Long vehiculeId);
}
