package com.dreameddeath.core.model.v2.meta;

import com.dreameddeath.core.model.v2.DocumentFlag;
import com.dreameddeath.core.model.v2.DocumentState;

import java.util.*;

public final class CouchbaseMetaInfo {
    private final String key;
    private final long cas;
    private final List<DocumentFlag> flags;
    private final int expiry;
    private final DocumentState state;
    private final String bucketName;
    private final long vbucketID;
    private final long vbucketUUID;
    private final long sequenceNumber;
    private final byte[] dbData;


    public CouchbaseMetaInfo(Builder builder) {
        this.key = builder.key;
        this.cas = builder.cas;
        this.flags = Collections.unmodifiableList(builder.flags);
        this.expiry = builder.expiry;
        this.state = builder.state;
        this.bucketName = builder.bucketName;
        this.vbucketID = builder.vbucketID;
        this.vbucketUUID = builder.vbucketUUID;
        this.sequenceNumber = builder.sequenceNumber;
        this.dbData = builder.dbData;
    }


    public String key() {
        return this.key;
    }

    public long cas() {
        return this.cas;
    }

    public Collection<DocumentFlag> flags() {
        return this.flags;
    }

    public int expiry() {
        return this.expiry;
    }

    public DocumentState state() {
        return this.state;
    }

    public Optional<String> bucketName() {
        return Optional.ofNullable(this.bucketName);
    }

    public long vBucketID() {
        return this.vbucketID;
    }

    public long vBucketUUID() {
        return this.vbucketUUID;
    }

    public long sequenceNumber() {
        return this.sequenceNumber;
    }

    public Optional<byte[]> dbData() {
        return Optional.ofNullable(this.dbData);
    }


    //Helpers
    public Integer dbSize(){
        return this.dbData!=null?this.dbData.length:null;
    }

    public Integer encodedFlags(){
        return DocumentFlag.pack(this.flags);
    }

    public boolean hasFlag(DocumentFlag flag){
        return this.flags.contains(flag);
    }

    public Builder toMutable(){
        return new Builder(this);
    }

    public static class Builder {
        private String key;
        private long cas = 0;
        private final List<DocumentFlag> flags = new ArrayList<>();
        private int expiry=0;
        private DocumentState state = DocumentState.NEW;
        private String bucketName=null;
        private long vbucketID=0;
        private long vbucketUUID=0;
        private long sequenceNumber=0;
        private byte[] dbData = null;


        public Builder(){
        }

        public Builder(CouchbaseMetaInfo orig){
            this.withKey(orig.key())
                    .withCas(orig.cas())
                    .withFlags(orig.flags())
                    .withExpiry(orig.expiry())
                    .withState(orig.state())
                    .withBucketName(orig.bucketName().orElse(null))
                    .withVbucketID(orig.vBucketID())
                    .withVbucketUUID(orig.vBucketUUID())
                    .withSequenceNumber(orig.sequenceNumber())
                    .withDbData(orig.dbData().orElse(null));
        }


        public CouchbaseMetaInfo create() {
            return new CouchbaseMetaInfo(this);
        }

        public static Builder newMeta(){
            return new Builder();
        }

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder withCas(long cas) {
            this.cas = cas;
            return this;
        }

        public Builder withFlags(Collection<DocumentFlag> flags) {
            this.flags.clear();
            this.flags.addAll(flags);
            return this;
        }

        public Builder withExpiry(int expiry) {
            this.expiry = expiry;
            return this;
        }

        public Builder withState(DocumentState state) {
            this.state = state;
            return this;
        }

        public Builder withBucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder withVbucketID(long vbucketID) {
            this.vbucketID = vbucketID;
            return this;
        }

        public Builder withVbucketUUID(long vbucketUUID) {
            this.vbucketUUID = vbucketUUID;
            return this;
        }

        public Builder withSequenceNumber(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder withDbData(byte[] dbData) {
            this.dbData = dbData;
            return this;
        }

        /*
            Helpers
         */

        public Builder toModified(){
            if(DocumentState.SYNC == this.state){
                this.state = DocumentState.DIRTY;
            }
            return this;
        }
        public Builder withEncodedFlags(Integer encodedFlags){
            return withFlags(DocumentFlag.unPack(encodedFlags));
        }

        public Builder addFlag(DocumentFlag flag) {
            this.flags.add(flag);
            return this;
        }

        public Builder removeFlag(DocumentFlag flag) {
            this.flags.remove(flag);
            return this;
        }

        public Builder addFlags(Collection<DocumentFlag> flags){
            this.flags.addAll(flags);
            return this;
        }

        public Builder addEncodedFlags(Integer encodedFlags){
            return addFlags(DocumentFlag.unPack(encodedFlags));
        }

        public Builder removeFlags(Collection<DocumentFlag> flags){
            this.flags.removeAll(flags);
            return this;
        }

    }

}
