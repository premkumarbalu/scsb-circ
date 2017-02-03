package org.recap.repository;


import org.recap.model.RequestInstitutionBibEntity;
import org.recap.model.RequestInstitutionBibPK;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface RequestInstitutionBibDetailsRepository extends PagingAndSortingRepository<RequestInstitutionBibEntity, RequestInstitutionBibPK> {

    RequestInstitutionBibEntity findByItemIdAndOwningInstitutionId(Integer itemId,Integer owningInstitutionId);

}
