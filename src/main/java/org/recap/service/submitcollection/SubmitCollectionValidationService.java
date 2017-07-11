package org.recap.service.submitcollection;

import org.recap.model.InstitutionEntity;
import org.recap.repository.InstitutionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by premkb on 11/7/17.
 */
@Service
public class SubmitCollectionValidationService {

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    public boolean validateInstitution(String institutionCode){
        InstitutionEntity institutionEntity = institutionDetailsRepository.findByInstitutionCode(institutionCode);
        if(institutionEntity != null){
            return true;
        } else {
            return false;
        }
    }
}
