package com.dreameddeath.installedbase.utils;

import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.installedbase.model.v1.InstalledBase;
import com.dreameddeath.installedbase.model.v1.common.InstalledStatus;
import com.dreameddeath.installedbase.model.v1.common.InstalledValue;
import com.dreameddeath.installedbase.model.v1.offer.InstalledAtomicOffer;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductService;
import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

    public static class Values{
        public static List<InstalledValue> findMatchingInstalledValues(List<InstalledValue> values, DateTime startDate, DateTime endDate){
            return values.stream()
                    .filter(value->!endDate.isBefore(value.getStartDate()) && !startDate.isAfter(value.getEndDate()))
                    .collect(Collectors.toList());
        }

        public static List<InstalledValue> findMatchingInstalledValues(List<InstalledValue> values, DateTime startDate,String valueStr){
            return values.stream()
                    .filter(value->startDate.isEqual(value.getStartDate()) && valueStr.equals(value.getValue()))
                    .collect(Collectors.toList());
        }

    }

    public static class Statuses{
        public static InstalledStatus getStatusFromHistory(final List<InstalledStatus> statuses,final DateTime refDate) {
            Preconditions.checkNotNull(refDate);
            DateTime oldestDate = IDateTimeService.MAX_TIME;
            for (InstalledStatus status : statuses) {

                if ((refDate.compareTo(status.getStartDate()) >= 0) &&
                        (refDate.compareTo(status.getEndDate()) < 0)) {
                    return status;
                }
                if (status.getStartDate().isBefore(oldestDate)) {
                    oldestDate = status.getStartDate();
                }
            }
            return buildInexistingStatus(oldestDate);
        }


        public static List<InstalledStatus> getMatchingStatuses(final List<InstalledStatus> statuses,final DateTime startDate, final DateTime endDate) {
            Preconditions.checkNotNull(startDate);
            Preconditions.checkNotNull(endDate);
            return statuses.stream().filter(status->isStatusOverlapping(status,startDate,endDate)).collect(Collectors.toList());
        }

        public static InstalledStatus buildInexistingStatus(DateTime startDate){
            InstalledStatus result = new InstalledStatus();
            result.setCode(InstalledStatus.Code.INEXISTING);
            result.setStartDate(IDateTimeService.MIN_TIME);
            result.setEndDate(startDate);
            return result;
        }

        public static boolean isStatusOverlapping(InstalledStatus source,InstalledStatus target){
            return isStatusOverlapping(source,target.getStartDate(),target.getEndDate());
        }

        public static boolean isStatusOverlapping(InstalledStatus source,DateTime startDate, DateTime endDate){
            return  source.getStartDate().compareTo(endDate)<0 && source.getEndDate().compareTo(startDate)>0
                    ||
                    startDate.compareTo(source.getEndDate())<0 && endDate.compareTo(source.getStartDate())>0
                    ;
        }
    }

}
