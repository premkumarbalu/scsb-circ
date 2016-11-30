package org.recap.repository;

import org.recap.model.HoldingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Created by chenchulakshmig on 6/13/16.
 */
public interface HoldingsDetailsRepository extends JpaRepository<HoldingsEntity, String> {

    HoldingsEntity findByHoldingsId(Integer holdingsId);

    Long countByOwningInstitutionId(Integer owningInstitutionId);

}
