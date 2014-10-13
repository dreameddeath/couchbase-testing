package com.dreameddeath.core.model.binary;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import com.dreameddeath.core.storage.CouchbaseConstants;

/**
 * Created by ceaj8230 on 12/10/2014.
 */
public class BinaryCouchbaseDocument extends BaseCouchbaseDocument {
    public BinaryCouchbaseDocument(){
        super(null);
        setBaseMeta(BinaryCouchbaseDocument.this.new BinaryMetaInfo());
    }

    public BinaryMetaInfo getBinaryMeta(){
        return (BinaryMetaInfo) getBaseMeta();
    }

    /**
     * Standard constructor
     * @param binaryDocumentType It has to be carefully chosen.
     *        if type == BINARY_FULL, it shouldn't be used (add this constructor is more designed to be a delta mode)
     *        if type == BINARY_PARTIAL_WITHOUT_CHECKSUM, the object will be used to normally add binary content
     *        if type == BINARY_PARTIAL_WITH_CHECKSUM, the object will be used to append a partial binary part
     */
    public BinaryCouchbaseDocument(BinaryDocumentType binaryDocumentType) {
        super(null);
        BinaryMetaInfo binaryMetaInfo = this.new BinaryMetaInfo();
        binaryMetaInfo.setBinaryDocumentType(binaryDocumentType);
        binaryMetaInfo.setEndingCheckSum(0);
        binaryMetaInfo.setLastWrittenSize(0);
    }

    /**
     * Incremental rating constructor
     * @param origDbSize the database size prior to the appending of the cdr
     * @param binaryDocumentType It has to be carefully chosen.
     *        if type == BINARY_FULL, it shouldn't be used (add this constructor is more designed to be a delta mode)
     *        if type == BINARY_PARTIAL_WITHOUT_CHECKSUM, the object will be used to normally add binary content
     *        if type == BINARY_PARTIAL_WITH_CHECKSUM, the object will be used to append a partial binary part
     */
    public BinaryCouchbaseDocument(Integer origDbSize,BinaryDocumentType binaryDocumentType){
        this(binaryDocumentType);
        getBaseMeta().setDbSize(origDbSize);
    }



    public class BinaryMetaInfo extends BaseMetaInfo {
        /// The document type is used during the Transcoder
        private BinaryDocumentType _binaryDocumentType;
        /// The check-sum of the last cdrs read to detect the error
        private int _endingCheckSum;
        /// The last append/written size
        private int _lastWrittenSize;

        /// Checksum Getter/Setter
        public int getEndingCheckSum(){ return _endingCheckSum;}
        public void setEndingCheckSum(int endingCheckSum){_endingCheckSum=endingCheckSum;}

        /// Last Written Size Getter/Setter
        public int getLastWrittenSize(){return _lastWrittenSize;}
        public void setLastWrittenSize(int appendedSize){_lastWrittenSize=appendedSize;}

        /// Getter of document Type
        public BinaryDocumentType getBinaryDocumentType(){return _binaryDocumentType; }
        public void setBinaryDocumentType(BinaryDocumentType binaryDocumentType){_binaryDocumentType=binaryDocumentType; }

    }


    /**
     *  Binary Document Types
     */
    public static enum BinaryDocumentType {
        BINARY_FULL("full"),
        BINARY_PARTIAL_WITH_CHECKSUM("partial_with_checksum"),
        BINARY_PARTIAL_WITHOUT_CHECKSUM("partiel_without_checksum");


        private String _value;

        BinaryDocumentType(String value){
            this._value = value;
        }

        @Override
        public String toString(){
            return _value;
        }
    }

}
