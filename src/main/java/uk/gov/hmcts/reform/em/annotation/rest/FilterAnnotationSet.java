package uk.gov.hmcts.reform.em.annotation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.em.annotation.rest.errors.ResourceNotFoundException;
import uk.gov.hmcts.reform.em.annotation.service.AnnotationSetService;
import uk.gov.hmcts.reform.em.annotation.service.dto.AnnotationSetDTO;

import java.util.Optional;

/**
 * REST controller for managing AnnotationSet.
 */
@RestController
@RequestMapping("/api")
public class FilterAnnotationSet {

    private final Logger log = LoggerFactory.getLogger(FilterAnnotationSet.class);

    private final AnnotationSetService annotationSetService;

    public FilterAnnotationSet(AnnotationSetService annotationSetService) {
        this.annotationSetService = annotationSetService;
    }

    @Operation(summary = "Filter an annotationSet", description = "A GET request to filter an annotationSetDTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Unauthorised"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
    })
    @GetMapping("/annotation-sets/filter")
    public ResponseEntity<AnnotationSetDTO> getAllAnnotationSets(@RequestParam("documentId") String documentId) {
        log.debug("REST request to get a page of AnnotationSets");
        Optional<AnnotationSetDTO> optionalAnnotationSetDTO = annotationSetService.findOneByDocumentId(documentId);
        return optionalAnnotationSetDTO
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Could not find annotation set for this document id#" + documentId));
    }
}
