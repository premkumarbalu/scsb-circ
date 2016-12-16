package org.recap.repository;

import org.recap.model.InstitutionEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface InstitutionDetailsRepository extends PagingAndSortingRepository<InstitutionEntity,Integer> {
    InstitutionEntity findByInstitutionId(Integer institutionId);
    InstitutionEntity findByInstitutionCode(String institutionCode);
    InstitutionEntity findByInstitutionName(String institutionName);
}
