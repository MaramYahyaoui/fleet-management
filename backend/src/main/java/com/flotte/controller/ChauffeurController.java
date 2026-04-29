package com.flotte.controller;

import com.flotte.dto.ApiResponseDTO;
import com.flotte.dto.ChauffeurDTO;
import com.flotte.service.ChauffeurService;
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

@CrossOrigin
@RestController
@RequestMapping("/api/chauffeurs")
@RequiredArgsConstructor
@Tag(name = "Chauffeurs", description = "Gestion des chauffeurs de la flotte")
public class ChauffeurController {

    private final ChauffeurService chauffeurService;

    // ─────────────────────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Lister tous les chauffeurs",
        description = "Retourne la liste de tous les chauffeurs. Filtrage optionnel par nom."
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
        content = @Content(schema = @Schema(implementation = ChauffeurDTO.class)))
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ChauffeurDTO>>> findAll(
            @Parameter(description = "Filtrer par nom du chauffeur (recherche partielle)")
            @RequestParam(required = false) String nom) {
        List<ChauffeurDTO> chauffeurs = nom != null
                ? chauffeurService.findByNom(nom)
                : chauffeurService.findAll();
        return ResponseEntity.ok(ApiResponseDTO.ok(chauffeurs));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Trouver un chauffeur par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chauffeur trouvé"),
        @ApiResponse(responseCode = "404", description = "Chauffeur introuvable", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ChauffeurDTO>> findById(
            @Parameter(description = "ID du chauffeur", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(chauffeurService.findById(id)));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET DISPONIBLES
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Lister les chauffeurs disponibles",
        description = "Retourne uniquement les chauffeurs qui ne sont pas en mission actuellement."
    )
    @ApiResponse(responseCode = "200", description = "Liste des chauffeurs disponibles")
    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponseDTO<List<ChauffeurDTO>>> findDisponibles() {
        return ResponseEntity.ok(ApiResponseDTO.ok(chauffeurService.findDisponibles()));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET LES PLUS ACTIFS
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Chauffeurs les plus actifs",
        description = "Retourne le classement des chauffeurs selon le nombre de missions effectuées et la distance totale parcourue."
    )
    @ApiResponse(responseCode = "200", description = "Classement récupéré")
    @GetMapping("/les-plus-actifs")
    public ResponseEntity<ApiResponseDTO<List<ChauffeurDTO>>> findLesPlasActifs() {
        return ResponseEntity.ok(ApiResponseDTO.ok(chauffeurService.findLesPlasActifs()));
    }

    // ─────────────────────────────────────────────────────────────
    //  POST CREATE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Créer un nouveau chauffeur")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Chauffeur créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides (validation échouée)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ChauffeurDTO>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données du chauffeur à créer",
                required = true,
                content = @Content(schema = @Schema(implementation = ChauffeurDTO.class))
            )
            @Valid @RequestBody ChauffeurDTO dto) {
        ChauffeurDTO created = chauffeurService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Chauffeur créé avec succès", created));
    }

    // ─────────────────────────────────────────────────────────────
    //  PUT UPDATE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Mettre à jour un chauffeur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chauffeur mis à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chauffeur introuvable", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ChauffeurDTO>> update(
            @Parameter(description = "ID du chauffeur", required = true) @PathVariable Long id,
            @Valid @RequestBody ChauffeurDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                "Chauffeur mis à jour", chauffeurService.update(id, dto)));
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Supprimer un chauffeur")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chauffeur supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Chauffeur introuvable", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(
            @Parameter(description = "ID du chauffeur à supprimer", required = true)
            @PathVariable Long id) {
        chauffeurService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Chauffeur supprimé avec succès", null));
    }
}