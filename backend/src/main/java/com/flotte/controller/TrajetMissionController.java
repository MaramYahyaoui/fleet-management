package com.flotte.controller;

import com.flotte.dto.ApiResponseDTO;
import com.flotte.dto.TrajetMissionDTO;
import com.flotte.service.TrajetMissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Tag(name = "Missions / Trajets", description = "Gestion des missions et trajets de la flotte")
public class TrajetMissionController {

    private final TrajetMissionService missionService;

    // ─────────────────────────────────────────────────────────────
    //  GET ALL (avec filtres)
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Lister toutes les missions",
        description = "Retourne toutes les missions. Filtres disponibles : statut, véhicule, chauffeur, ou période (debut + fin). Un seul filtre est appliqué à la fois selon la priorité : vehiculeId > chauffeurId > statut > période."
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
        content = @Content(schema = @Schema(implementation = TrajetMissionDTO.class)))
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TrajetMissionDTO>>> findAll(
            @Parameter(description = "Filtrer par statut (EN_COURS, TERMINEE, ANNULEE)")
            @RequestParam(required = false) String statut,
            @Parameter(description = "Filtrer par ID du véhicule")
            @RequestParam(required = false) Long vehiculeId,
            @Parameter(description = "Filtrer par ID du chauffeur")
            @RequestParam(required = false) Long chauffeurId,
            @Parameter(description = "Date de début (format : yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @Parameter(description = "Date de fin (format : yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<TrajetMissionDTO> missions;
        if (vehiculeId != null) {
            missions = missionService.findByVehicule(vehiculeId);
        } else if (chauffeurId != null) {
            missions = missionService.findByChauffeur(chauffeurId);
        } else if (statut != null) {
            missions = missionService.findByStatut(statut);
        } else if (debut != null && fin != null) {
            missions = missionService.findByPeriode(debut, fin);
        } else {
            missions = missionService.findAll();
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(missions));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Trouver une mission par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mission trouvée"),
        @ApiResponse(responseCode = "404", description = "Mission introuvable", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TrajetMissionDTO>> findById(
            @Parameter(description = "ID de la mission", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(missionService.findById(id)));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET EN COURS
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Missions en cours",
        description = "Retourne uniquement les missions dont le statut est EN_COURS."
    )
    @ApiResponse(responseCode = "200", description = "Liste des missions en cours")
    @GetMapping("/en-cours")
    public ResponseEntity<ApiResponseDTO<List<TrajetMissionDTO>>> findEnCours() {
        return ResponseEntity.ok(ApiResponseDTO.ok(missionService.findEnCours()));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY VEHICULE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Missions d'un véhicule")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Missions récupérées"),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<ApiResponseDTO<List<TrajetMissionDTO>>> findByVehicule(
            @Parameter(description = "ID du véhicule", required = true)
            @PathVariable Long vehiculeId) {
        return ResponseEntity.ok(ApiResponseDTO.ok(missionService.findByVehicule(vehiculeId)));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY CHAUFFEUR
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Missions d'un chauffeur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Missions récupérées"),
        @ApiResponse(responseCode = "404", description = "Chauffeur introuvable", content = @Content)
    })
    @GetMapping("/chauffeur/{chauffeurId}")
    public ResponseEntity<ApiResponseDTO<List<TrajetMissionDTO>>> findByChauffeur(
            @Parameter(description = "ID du chauffeur", required = true)
            @PathVariable Long chauffeurId) {
        return ResponseEntity.ok(ApiResponseDTO.ok(missionService.findByChauffeur(chauffeurId)));
    }

    // ─────────────────────────────────────────────────────────────
    //  POST CREATE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Créer une nouvelle mission")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Mission créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<TrajetMissionDTO>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de la mission à créer",
                required = true,
                content = @Content(schema = @Schema(implementation = TrajetMissionDTO.class))
            )
            @Valid @RequestBody TrajetMissionDTO dto) {
        TrajetMissionDTO created = missionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Mission créée avec succès", created));
    }

    // ─────────────────────────────────────────────────────────────
    //  PUT UPDATE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Mettre à jour une mission")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mission mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Mission introuvable", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<TrajetMissionDTO>> update(
            @Parameter(description = "ID de la mission", required = true) @PathVariable Long id,
            @Valid @RequestBody TrajetMissionDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                "Mission mise à jour", missionService.update(id, dto)));
    }

    // ─────────────────────────────────────────────────────────────
    //  PATCH TERMINER
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Terminer une mission",
        description = "Marque la mission comme TERMINEE. Paramètre optionnel 'distance' pour corriger la distance finale (en km)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mission terminée avec succès"),
        @ApiResponse(responseCode = "404", description = "Mission introuvable", content = @Content)
    })
    @PatchMapping("/{id}/terminer")
    public ResponseEntity<ApiResponseDTO<TrajetMissionDTO>> terminer(
            @Parameter(description = "ID de la mission", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Distance finale en km (optionnel). Ex: {\"distance\": 120.5}",
                required = false
            )
            @RequestBody(required = false) Map<String, Double> body) {
        Double distanceFinale = body != null ? body.get("distance") : null;
        return ResponseEntity.ok(ApiResponseDTO.ok(
                "Mission terminée", missionService.terminerMission(id, distanceFinale)));
    }

    // ─────────────────────────────────────────────────────────────
    //  PATCH ANNULER
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Annuler une mission",
        description = "Marque la mission comme ANNULEE. Impossible d'annuler une mission déjà terminée."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mission annulée"),
        @ApiResponse(responseCode = "404", description = "Mission introuvable", content = @Content)
    })
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ApiResponseDTO<TrajetMissionDTO>> annuler(
            @Parameter(description = "ID de la mission", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                "Mission annulée", missionService.annulerMission(id)));
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Supprimer une mission")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Mission supprimée"),
        @ApiResponse(responseCode = "404", description = "Mission introuvable", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(
            @Parameter(description = "ID de la mission à supprimer", required = true)
            @PathVariable Long id) {
        missionService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Mission supprimée", null));
    }
}