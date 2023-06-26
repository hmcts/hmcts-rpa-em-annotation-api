package uk.gov.hmcts.reform.em.annotation.service;

import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.em.annotation.service.dto.AnnotationDTO;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CcdService {

    private final Logger log = LoggerFactory.getLogger(CcdService.class);
    private final CoreCaseDataApi coreCaseDataApi;

    private final AuthTokenGenerator authTokenGenerator;

    public CcdService(
            CoreCaseDataApi coreCaseDataApi,
            AuthTokenGenerator authTokenGenerator
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
    }

    public AnnotationDTO fetchAppellantDetails(AnnotationDTO annotationDTO, String authorisation) {
        if (Objects.isNull(annotationDTO.getCaseId())) {
            return annotationDTO;
        }
        if (Objects.nonNull(annotationDTO.getAppellant())) {
            return annotationDTO;
        }
        if (!(annotationDTO.getJurisdiction().equals("SSCS") || annotationDTO.getJurisdiction().equals("IA"))) {
            return annotationDTO;
        }
        CaseDetails caseDetails = getCaseDetails(authorisation, authTokenGenerator.generate(), annotationDTO.getCaseId());
        Map<String, Map<String, String> > appellant = (Map<String, Map<String, String>>) caseDetails.getData().get("appellant");
        Map<String, String> name = appellant.get("name");

        annotationDTO.setAppellant(name.values()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        return annotationDTO;
    }

    public CaseDetails getCaseDetails(String authorisation, String serviceAuthorisation, String caseId) {
        String serviceAuth = authTokenGenerator.generate();
        CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation,
                serviceAuth, caseId);
        log.info("caseDetails value is {}", serviceAuth);
        log.info("caseDetails.data value is {}", caseDetails.getData());
        return caseDetails;

    }
}


