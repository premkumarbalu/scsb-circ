package org.recap.repository;

import org.recap.model.DeaccessionItemChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by akulak on 26/2/18 .
 */
@Repository
public interface DeaccesionItemChangeLogDetailsRepository extends JpaRepository<DeaccessionItemChangeLog,Integer> {

}
