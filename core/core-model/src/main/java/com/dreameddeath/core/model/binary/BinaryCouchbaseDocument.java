/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.model.binary;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 12/10/2014.
 */
public class BinaryCouchbaseDocument extends CouchbaseDocument {
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
        binaryMetaInfo.setCheckSum(0);
        binaryMetaInfo.setLastWrittenSize(0);
        setBaseMeta(binaryMetaInfo);
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
        private int _checkSum;
        /// The last append/written size
        private int _lastWrittenSize;

        /// Checksum Getter/Setter
        public int getCheckSum(){ return _checkSum;}
        public void setCheckSum(int checkSum){_checkSum = checkSum;}

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
    public enum BinaryDocumentType {
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
