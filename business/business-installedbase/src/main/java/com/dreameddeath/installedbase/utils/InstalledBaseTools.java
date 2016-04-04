package com.dreameddeath.installedbase.utils;

import com.dreameddeath.installedbase.model.InstalledBase;
import com.dreameddeath.installedbase.model.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.productservice.InstalledProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 24/03/2016.
 */
public class InstalledBaseTools {
    public static class InstalledBaseIndexer{
        private static final Logger LOG= LoggerFactory.getLogger(InstalledBaseIndexer.class);
        private final InstalledBase installedBase;
        private final Map<String,InstalledProductService> installedProductServiceMap = new TreeMap<>();
        private final Map<String,InstalledOffer> installedOfferPerIdMap = new TreeMap<>();

        public InstalledBaseIndexer(InstalledBase src){
            installedBase = src;

            for(InstalledProductService ps:src.getPsList()){
                installedProductServiceMap.put(ps.getId(),ps);
            }

            for(InstalledOffer offer:src.getOffers()){
                installedOfferPerIdMap.put(offer.getId(),offer);
                if(offer instanceof InstalledAtomicOffer){
                    String psId=((InstalledAtomicOffer)offer).getPs();
                    if(installedProductServiceMap.containsKey(psId)){
                        LOG.error("Cannot find ps {} for installed base offer {}",psId,offer.getId());
                    }
                }
            }
        }

        public List<InstalledOffer> findSortestPath(String idSrc,String idTarget){
            return Collections.emptyList();
        }

        public List<InstalledOffer> findPath(String idSrc,String idParent){
            List<InstalledOffer> result = new LinkedList<>();
            InstalledOffer current=installedOfferPerIdMap.get(idSrc);
            while(current!=null){
                result.add(current);
                if(current.getId().equals(idParent)){
                    break;
                }
                if(current.getParent()!=null) {
                    current = installedOfferPerIdMap.get(current.getParent());
                }
                else{
                    current=null;
                }
            }

            if(current!=null){
                return result;
            }
            else{
                return Collections.emptyList();
            }
        }

        public InstalledOffer getInstalledOffer(String id){
            return installedOfferPerIdMap.get(id);
        }

        public InstalledProductService getInstalledProductService(String id){
            return installedProductServiceMap.get(id);
        }

        public void addToInstalledBase(InstalledOffer offer){
            installedBase.addOffers(offer);
            installedOfferPerIdMap.put(offer.getId(),offer);
            /*if(offer instanceof InstalledAtomicOffer){
                InstalledProductService ps=((InstalledAtomicOffer)offer).getProduct();
                installedProductServiceMap.put(ps.getId(),ps);
            }*/
        }

        public void addToInstalledBase(InstalledProductService ps){
            installedProductServiceMap.put(ps.getId(),ps);
        }
    }

}
