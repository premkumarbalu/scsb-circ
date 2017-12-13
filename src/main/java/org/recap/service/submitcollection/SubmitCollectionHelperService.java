package org.recap.service.submitcollection;

import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.recap.service.common.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by premkb on 7/12/17.
 */
@Service
public class SubmitCollectionHelperService {

    @Autowired
    private RepositoryService repositoryService;

    public BibliographicEntity getBibliographicEntityIfExist(String owningInstitutionBibId,Integer owningInstitutionId){
        BibliographicEntity bibliographicEntityList = getRepositoryService().getBibliographicDetailsRepository().findByOwningInstitutionIdAndOwningInstitutionBibId(owningInstitutionId,owningInstitutionBibId);
        return bibliographicEntityList;
    }

    public void attachItemToExistingBib(BibliographicEntity existingBibliographicEntity, BibliographicEntity incomingBibliographicEntity){
        existingBibliographicEntity.getItemEntities().addAll(incomingBibliographicEntity.getItemEntities());
        existingBibliographicEntity.setDeleted(false);
        String incomingOwningInstitutionHoldingsId = incomingBibliographicEntity.getHoldingsEntities().get(0).getOwningInstitutionHoldingsId();
        boolean isHoldingsExist = isHoldingsExist(existingBibliographicEntity,incomingOwningInstitutionHoldingsId);
        if (!isHoldingsExist){
            attachItemToHolding(existingBibliographicEntity,incomingBibliographicEntity);
        }
    }

    private boolean isHoldingsExist(BibliographicEntity bibliographicEntity,String owningInstitutionHoldingsId){
        for(HoldingsEntity holdingsEntity:bibliographicEntity.getHoldingsEntities()){
            if(owningInstitutionHoldingsId.equals(holdingsEntity.getOwningInstitutionHoldingsId())){
                return true;
            }
        }
        return false;
    }

    private void attachItemToHolding(BibliographicEntity existingBibliographicEntity, BibliographicEntity incomingBibliographicEntity){
        existingBibliographicEntity.getHoldingsEntities().add(incomingBibliographicEntity.getHoldingsEntities().get(0));
    }

    public Map<String,Map<String,ItemEntity>> getHoldingItemIdMap(BibliographicEntity bibliographicEntity){
        Map<String,Map<String,ItemEntity>> holdingItemMap = new HashMap<>();
        for(HoldingsEntity holdingsEntity:bibliographicEntity.getHoldingsEntities()){
            Map<String,ItemEntity> itemEntityMap = new HashMap<>();
            for(ItemEntity itemEntity:holdingsEntity.getItemEntities()){
                itemEntityMap.put(itemEntity.getOwningInstitutionItemId(),itemEntity);
            }
            holdingItemMap.put(holdingsEntity.getOwningInstitutionHoldingsId(),itemEntityMap);
        }
        return holdingItemMap;
    }

    public String getBibliographicIdsInString(List<BibliographicEntity> bibliographicEntityList){
        List<String> owningInstitutionBibIdList = bibliographicEntityList.stream().map(BibliographicEntity::getOwningInstitutionBibId).collect(Collectors.toList());
        return owningInstitutionBibIdList.stream().collect(Collectors.joining(","));
    }

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    public void setRepositoryService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }
}
