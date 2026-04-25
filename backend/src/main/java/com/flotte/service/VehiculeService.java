package com.flotte.service;

import com.flotte.dto.AlertMaintenanceDTO;
import com.flotte.dto.VehiculeDTO;
import com.flotte.entity.Vehicule;
import com.flotte.exception.BusinessException;
import com.flotte.exception.ResourceNotFoundException;
import com.flotte.repository.ConsommationRepository;
import com.flotte.repository.TrajetMissionRepository;
import com.flotte.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final TrajetMissionRepository trajetRepository;
    private final ConsommationRepository consommationRepository;

    @Value("${fleet.maintenance.seuil-kilometrage:10000}")
    private Integer seuilMaintenance;

    public List<VehiculeDTO> findAll() {
        return vehiculeRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VehiculeDTO findById(Long id) {
        return vehiculeRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule", id));
    }

    public VehiculeDTO findByImmatriculation(String immatriculation) {
        return vehiculeRepository.findByImmatriculation(immatriculation)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Véhicule avec immatriculation " + immatriculation + " introuvable"));
    }

    public List<VehiculeDTO> findByStatut(String statut) {
        return vehiculeRepository.findByStatut(statut).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VehiculeDTO> findActifs() {
        return vehiculeRepository.findVehiculesActifs().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehiculeDTO create(VehiculeDTO dto) {
        if (vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new BusinessException(
                    "Un véhicule avec l'immatriculation " + dto.getImmatriculation() + " existe déjà");
        }
        Vehicule vehicule = fromDTO(dto);
        return toDTO(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public VehiculeDTO update(Long id, VehiculeDTO dto) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule", id));

        // Vérifier unicité immatriculation si elle change
        if (!vehicule.getImmatriculation().equals(dto.getImmatriculation())
                && vehiculeRepository.existsByImmatriculation(dto.getImmatriculation())) {
            throw new BusinessException(
                    "L'immatriculation " + dto.getImmatriculation() + " est déjà utilisée");
        }

        vehicule.setImmatriculation(dto.getImmatriculation());
        vehicule.setModele(dto.getModele());
        vehicule.setType(dto.getType());
        vehicule.setKilometrage(dto.getKilometrage());
        vehicule.setStatut(dto.getStatut());

        return toDTO(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public VehiculeDTO updateStatut(Long id, String statut) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule", id));
        vehicule.setStatut(statut);
        return toDTO(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public VehiculeDTO updateKilometrage(Long id, Integer kilometrage) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule", id));
        if (kilometrage < vehicule.getKilometrage()) {
            throw new BusinessException(
                    "Le nouveau kilométrage (" + kilometrage + ") ne peut pas être inférieur à l'actuel ("
                            + vehicule.getKilometrage() + ")");
        }
        vehicule.setKilometrage(kilometrage);
        return toDTO(vehiculeRepository.save(vehicule));
    }

    @Transactional
    public void delete(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Véhicule", id);
        }
        if (trajetRepository.vehiculeEnMission(id)) {
            throw new BusinessException("Impossible de supprimer un véhicule en mission");
        }
        vehiculeRepository.deleteById(id);
    }

    public List<AlertMaintenanceDTO> getAlertesMaintenance() {
        return vehiculeRepository.findVehiculesPourMaintenance(seuilMaintenance).stream()
                .map(v -> buildAlerte(v, seuilMaintenance))
                .collect(Collectors.toList());
    }

    public List<AlertMaintenanceDTO> getAlertesMaintenancePersonnalise(Integer seuil) {
        return vehiculeRepository.findVehiculesPourMaintenance(seuil).stream()
                .map(v -> buildAlerte(v, seuil))
                .collect(Collectors.toList());
    }

    public List<VehiculeDTO> getVehiculesLesPlasActifs() {
        return vehiculeRepository.findVehiculesLasPlusActifs().stream()
                .map(this::toDTOAvecStats)
                .collect(Collectors.toList());
    }

    // ========== Mappers ==========

    public VehiculeDTO toDTO(Vehicule v) {
        return VehiculeDTO.builder()
                .id(v.getId())
                .immatriculation(v.getImmatriculation())
                .modele(v.getModele())
                .type(v.getType())
                .kilometrage(v.getKilometrage())
                .statut(v.getStatut())
                .build();
    }

    private VehiculeDTO toDTOAvecStats(Vehicule v) {
        VehiculeDTO dto = toDTO(v);
        if (v.getTrajets() != null) {
            dto.setNombreMissions((long) v.getTrajets().size());
            dto.setDistanceTotale(v.getTrajets().stream()
                    .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0)
                    .sum());
        }
        if (v.getConsommations() != null) {
            dto.setCoutCarburantTotal(v.getConsommations().stream()
                    .mapToDouble(c -> c.getCoutTotal() != null ? c.getCoutTotal() : 0)
                    .sum());
        }
        return dto;
    }

    private Vehicule fromDTO(VehiculeDTO dto) {
        return Vehicule.builder()
                .immatriculation(dto.getImmatriculation())
                .modele(dto.getModele())
                .type(dto.getType())
                .kilometrage(dto.getKilometrage() != null ? dto.getKilometrage() : 0)
                .statut(dto.getStatut())
                .build();
    }

    private AlertMaintenanceDTO buildAlerte(Vehicule v, Integer seuil) {
        int depassement = v.getKilometrage() - seuil;
        String niveau = depassement > 20000 ? "CRITIQUE"
                : depassement > 5000 ? "ATTENTION"
                : "INFO";
        return AlertMaintenanceDTO.builder()
                .vehiculeId(v.getId())
                .immatriculation(v.getImmatriculation())
                .modele(v.getModele())
                .kilometrageActuel(v.getKilometrage())
                .seuilMaintenance(seuil)
                .depassement(depassement)
                .niveauAlerte(niveau)
                .build();
    }
}
