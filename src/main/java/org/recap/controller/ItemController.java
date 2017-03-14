package org.recap.controller;

import org.recap.model.BibliographicEntity;
import org.recap.model.ItemEntity;
import org.recap.repository.ItemDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@RestController
@RequestMapping("/item")
public class ItemController {
    private final ItemDetailsRepository itemDetailsRepository;


    @Autowired
    public ItemController(ItemDetailsRepository itemDetailsRepository) {
        this.itemDetailsRepository = itemDetailsRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value ="/findByBarcodeIn")
    public List<ItemEntity> findByBarcodeIn(String barcodes){

        List<ItemEntity> itemEntityList = itemDetailsRepository.findByBarcodeInAndComplete(splitStringAndGetList(barcodes));
        for (Iterator<ItemEntity> itemEntityIterator = itemEntityList.iterator(); itemEntityIterator.hasNext(); ) {
            ItemEntity itemEntity = itemEntityIterator.next();
            for(BibliographicEntity bibliographicEntity : itemEntity.getBibliographicEntities()){
                bibliographicEntity.setItemEntities(null);
                bibliographicEntity.setHoldingsEntities(null);
            }
            itemEntity.setHoldingsEntities(null);
        }
        return itemEntityList;
    }

    public List<String> splitStringAndGetList(String itemBarcodes){
        itemBarcodes = itemBarcodes.replaceAll("\\[","").replaceAll("\\]","");
        String[] splittedString = itemBarcodes.split(",");
        List<String> stringList = new ArrayList<>();
        for(String barcode : splittedString){
            stringList.add(barcode.trim());
        }
        return stringList;
    }
}
