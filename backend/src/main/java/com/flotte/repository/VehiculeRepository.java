package com.flotte.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.flotte.entity.Vehicule;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    Optional<Vehicule> findByImmatriculation(String immatriculation);

    boolean existsByImmatriculation(String immatriculation);

    List<Vehicule> findByStatut(String statut);

    List<Vehicule> findByType(String type);

    // Véhicules nécessitant une maintenance (kilométrage >= seuil)
    @Query("SELECT v FROM Vehicule v WHERE v.kilometrage >= :seuil ORDER BY v.kilometrage DESC")
    List<Vehicule> findVehiculesPourMaintenance(@Param("seuil") Integer seuil);

    // Véhicules les plus actifs (par nombre de missions)
    @Query("SELECT v FROM Vehicule v LEFT JOIN v.trajets t GROUP BY v ORDER BY COUNT(t) DESC")
    List<Vehicule> findVehiculesLasPlusActifs();

    // Véhicules actifs (statut = DISPONIBLE ou EN_MISSION)
    @Query("SELECT v FROM Vehicule v WHERE v.statut IN ('DISPONIBLE', 'EN_MISSION')")
    List<Vehicule> findVehiculesActifs();

    // Recherche par modèle (insensible à la casse)
    List<Vehicule> findByModeleContainingIgnoreCase(String modele);
}

