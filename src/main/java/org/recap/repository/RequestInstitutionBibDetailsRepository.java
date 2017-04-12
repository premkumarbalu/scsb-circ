package org.recap.repository;


import org.recap.model.RequestInstitutionBibEntity;
import org.recap.model.RequestInstitutionBibPK;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by hemalathas on 22/6/16.
 */
public interface RequestInstitutionBibDetailsRepository extends PagingAndSortingRepository<RequestInstitutionBibEntity, RequestInstitutionBibPK> {

    /**
     * Find by item id and owning institution id request institution bib entity.
     *
     * @param itemId              the item id
     * @param owningInstitutionId the owning institution id
     * @return the request institution bib entity
     */
    RequestInstitutionBibEntity findByItemIdAndOwningInstitutionId(Integer itemId,Integer owningInstitutionId);

}
