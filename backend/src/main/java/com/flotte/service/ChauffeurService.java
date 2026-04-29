package com.flotte.service;

import com.flotte.dto.ChauffeurDTO;
import com.flotte.entity.Chauffeur;
import com.flotte.exception.BusinessException;
import com.flotte.exception.ResourceNotFoundException;
import com.flotte.repository.ChauffeurRepository;
import com.flotte.repository.TrajetMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChauffeurService {

    private final ChauffeurRepository chauffeurRepository;
    private final TrajetMissionRepository trajetRepository;

    public List<ChauffeurDTO> findAll() {
        return chauffeurRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ChauffeurDTO findById(Long id) {
        return chauffeurRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", id));
    }

    public List<ChauffeurDTO> findDisponibles() {
        return chauffeurRepository.findChauffeursdisponibles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ChauffeurDTO> findLesPlasActifs() {
        return chauffeurRepository.findChauffeursLesPlasActifs().stream()
                .map(this::toDTOAvecStats)
                .collect(Collectors.toList());
    }

    public List<ChauffeurDTO> findByNom(String nom) {
        return chauffeurRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChauffeurDTO create(ChauffeurDTO dto) {
        if (chauffeurRepository.existsByPermis(dto.getPermis())) {
            throw new BusinessException(
                    "Un chauffeur avec le permis " + dto.getPermis() + " existe déjà");
        }
        return toDTO(chauffeurRepository.save(fromDTO(dto)));
    }

    @Transactional
    public ChauffeurDTO update(Long id, ChauffeurDTO dto) {
        Chauffeur chauffeur = chauffeurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chauffeur", id));

        if (!chauffeur.getPermis().equals(dto.getPermis())
                && chauffeurRepository.existsByPermis(dto.getPermis())) {
            throw new BusinessException("Le numéro de permis " + dto.getPermis() + " est déjà utilisé");
        }

        chauffeur.setNom(dto.getNom());
        chauffeur.setPermis(dto.getPermis());
        chauffeur.setExperience(dto.getExperience());
        return toDTO(chauffeurRepository.save(chauffeur));
    }

    @Transactional
    public void delete(Long id) {
        if (!chauffeurRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chauffeur", id);
        }
        if (trajetRepository.chauffeurAMissionEnCours(id)) {
            throw new BusinessException("Impossible de supprimer un chauffeur ayant une mission en cours");
        }
        chauffeurRepository.deleteById(id);
    }

    // ========== Mappers ==========

    public ChauffeurDTO toDTO(Chauffeur c) {
        return ChauffeurDTO.builder()
                .id(c.getId())
                .nom(c.getNom())
                .permis(c.getPermis())
                .experience(c.getExperience())
                .build();
    }

    private ChauffeurDTO toDTOAvecStats(Chauffeur c) {
        ChauffeurDTO dto = toDTO(c);
        if (c.getTrajets() != null) {
            dto.setNombreMissions((long) c.getTrajets().size());
            dto.setDistanceTotale(c.getTrajets().stream()
                    .mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0)
                    .sum());
        }
        return dto;
    }

    private Chauffeur fromDTO(ChauffeurDTO dto) {
        return Chauffeur.builder()
                .nom(dto.getNom())
                .permis(dto.getPermis())
                .experience(dto.getExperience())
                .build();
    }
}
