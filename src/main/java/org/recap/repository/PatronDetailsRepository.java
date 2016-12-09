package org.recap.repository;


import org.recap.model.PatronEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * Created by rajeshbabuk on 28/10/16.
 */
public interface PatronDetailsRepository extends JpaRepository<PatronEntity, Integer> {

    public PatronEntity findByInstitutionIdentifier(@Param("institutionIdentifier") String institutionIdentifier);
}
