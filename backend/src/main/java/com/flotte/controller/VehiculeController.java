package com.flotte.controller;

import com.flotte.dto.AlertMaintenanceDTO;
import com.flotte.dto.ApiResponseDTO;
import com.flotte.dto.VehiculeDTO;
import com.flotte.service.VehiculeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
@Tag(name = "Véhicules", description = "Gestion des véhicules de la flotte")
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @Operation(
        summary = "Lister tous les véhicules",
        description = "Retourne la liste complète des véhicules. Filtrage optionnel par statut (ex: ACTIF, EN_PANNE, EN_MAINTENANCE)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = VehiculeDTO.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<VehiculeDTO>>> findAll(
            @Parameter(description = "Filtrer par statut du véhicule (ACTIF, EN_PANNE, EN_MAINTENANCE)")
            @RequestParam(required = false) String statut,
            @Parameter(description = "Filtrer par type de véhicule")
            @RequestParam(required = false) String type) {

        List<VehiculeDTO> vehicules;
        if (statut != null) {
            vehicules = vehiculeService.findByStatut(statut);
        } else {
            vehicules = vehiculeService.findAll();
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(vehicules));
    }

    
    @Operation(summary = "Trouver un véhicule par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Véhicule trouvé"),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VehiculeDTO>> findById(
            @Parameter(description = "ID du véhicule", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(vehiculeService.findById(id)));
    }

   
    @Operation(summary = "Trouver un véhicule par immatriculation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Véhicule trouvé"),
        @ApiResponse(responseCode = "404", description = "Aucun véhicule avec cette immatriculation", content = @Content)
    })
    @GetMapping("/immatriculation/{immatriculation}")
    public ResponseEntity<ApiResponseDTO<VehiculeDTO>> findByImmatriculation(
            @Parameter(description = "Numéro d'immatriculation (ex: TN-1234-AB)", required = true)
            @PathVariable String immatriculation) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                vehiculeService.findByImmatriculation(immatriculation)));
    }

 
    @Operation(summary = "Lister les véhicules actifs")
    @ApiResponse(responseCode = "200", description = "Liste des véhicules actifs")
    @GetMapping("/actifs")
    public ResponseEntity<ApiResponseDTO<List<VehiculeDTO>>> findActifs() {
        return ResponseEntity.ok(ApiResponseDTO.ok(vehiculeService.findActifs()));
    }

    @Operation(
        summary = "Véhicules les plus actifs",
        description = "Retourne le classement des véhicules selon le nombre de missions effectuées."
    )
    @ApiResponse(responseCode = "200", description = "Classement récupéré")
    @GetMapping("/les-plus-actifs")
    public ResponseEntity<ApiResponseDTO<List<VehiculeDTO>>> findLesPlasActifs() {
        return ResponseEntity.ok(ApiResponseDTO.ok(vehiculeService.getVehiculesLesPlasActifs()));
    }


    @Operation(
        summary = "Alertes de maintenance préventive",
        description = "Retourne les véhicules dont le kilométrage approche du seuil de maintenance. " +
                      "Seuil par défaut : 5000 km. Paramètre optionnel pour personnaliser le seuil."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Alertes récupérées avec succès")
    })
    @GetMapping("/maintenance/alertes")
    public ResponseEntity<ApiResponseDTO<List<AlertMaintenanceDTO>>> getAlertesMaintenance(
            @Parameter(description = "Seuil kilométrique personnalisé (par défaut : 5000)")
            @RequestParam(required = false) Integer seuil) {
        List<AlertMaintenanceDTO> alertes = seuil != null
                ? vehiculeService.getAlertesMaintenancePersonnalise(seuil)
                : vehiculeService.getAlertesMaintenance();
        return ResponseEntity.ok(ApiResponseDTO.ok(alertes));
    }

    
    @Operation(summary = "Créer un nouveau véhicule")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Véhicule créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides (validation échouée)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<VehiculeDTO>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données du véhicule à créer",
                required = true,
                content = @Content(schema = @Schema(implementation = VehiculeDTO.class))
            )
            @Valid @RequestBody VehiculeDTO dto) {
        VehiculeDTO created = vehiculeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Véhicule créé avec succès", created));
    }

 
    @Operation(summary = "Mettre à jour un véhicule")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Véhicule mis à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<VehiculeDTO>> update(
            @Parameter(description = "ID du véhicule", required = true) @PathVariable Long id,
            @Valid @RequestBody VehiculeDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                "Véhicule mis à jour", vehiculeService.update(id, dto)));
    }

  
    @Operation(
        summary = "Modifier le statut d'un véhicule",
        description = "Met à jour uniquement le champ statut. Valeurs acceptées : ACTIF, EN_PANNE, EN_MAINTENANCE."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
        @ApiResponse(responseCode = "400", description = "Champ 'statut' manquant ou vide", content = @Content),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @PatchMapping("/{id}/statut")
    public ResponseEntity<ApiResponseDTO<VehiculeDTO>> updateStatut(
            @Parameter(description = "ID du véhicule", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statut = body.get("statut");
        if (statut == null || statut.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Le champ 'statut' est requis"));
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(vehiculeService.updateStatut(id, statut)));
    }


    @Operation(
        summary = "Mettre à jour le kilométrage d'un véhicule",
        description = "Met à jour uniquement le kilométrage. La valeur doit être supérieure ou égale à 0."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kilométrage mis à jour"),
        @ApiResponse(responseCode = "400", description = "Champ 'kilometrage' manquant", content = @Content),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @PatchMapping("/{id}/kilometrage")
    public ResponseEntity<ApiResponseDTO<VehiculeDTO>> updateKilometrage(
            @Parameter(description = "ID du véhicule", required = true) @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Integer kilometrage = body.get("kilometrage");
        if (kilometrage == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("Le champ 'kilometrage' est requis"));
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(vehiculeService.updateKilometrage(id, kilometrage)));
    }

   
    @Operation(summary = "Supprimer un véhicule")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Véhicule supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(
            @Parameter(description = "ID du véhicule à supprimer", required = true)
            @PathVariable Long id) {
        vehiculeService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Véhicule supprimé avec succès", null));
    }
}