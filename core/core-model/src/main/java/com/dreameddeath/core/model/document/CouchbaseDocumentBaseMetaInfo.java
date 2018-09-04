package com.dreameddeath.core.model.document;

import com.dreameddeath.core.model.v2.DocumentFlag;
import com.dreameddeath.core.model.v2.DocumentState;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class CouchbaseDocumentBaseMetaInfo {
    private String  key;
    private long    cas;
    private Boolean isLocked;
    private Integer dbDocSize=null;
    private long    vbucketID=0;
    private long    vbucketUUID=0;
    private long    sequenceNumber=0;

    private Set<DocumentFlag> flags = EnumSet.noneOf(DocumentFlag.class);
    private int expiry;
    private DocumentState docState = DocumentState.NEW;
    private String bucketName;
    private byte[] dbData=null;
    private long   updatedFromCas;
    private boolean isFrozen = false;

    public CouchbaseDocumentBaseMetaInfo() {
    }

    public final String getKey(){
        return key;
    }

    public final void setKey(String key){
        this.key=key;
    }

    public final long getCas(){
        return cas;
    }

    public final void setCas(long cas){
        this.cas = cas;
    }

    public final Boolean getIsLocked(){
        return isLocked;
    }

    public final void setIsLocked(Boolean isLocked){
        this.isLocked = isLocked;
    }

    public final Integer getDbSize(){
        if(dbData!=null){
            return dbData.length;
        }
        else{
            return dbDocSize;
        }
    }

    public final void setDbSize(Integer docSize){
        this.dbDocSize = docSize;
    }

    public final Collection<DocumentFlag> getFlags(){
        return flags;
    }

    public final Integer getEncodedFlags(){
        return DocumentFlag.pack(flags);
    }

    public final void setEncodedFlags(Integer encodedFlags){
        this.flags.clear();
        this.flags.addAll(DocumentFlag.unPack(encodedFlags));
    }

    public final void setFlags(Collection<DocumentFlag> flags){
        this.flags.clear();
        this.flags.addAll(flags);
    }

    public final void addEncodedFlags(Integer encodedFlags){
        flags.addAll(DocumentFlag.unPack(encodedFlags));
    }

    public final void addFlag(DocumentFlag flag){
        flags.add(flag);
    }

    public final void addFlags(Collection<DocumentFlag> flags){
        this.flags.addAll(flags);
    }

    public final void removeFlag(DocumentFlag flag){
        flags.remove(flag);
    }

    public final void removeFlags(Collection<DocumentFlag> flags){
        this.flags.removeAll(flags);
    }

    public boolean hasFlag(DocumentFlag flag){
        return flags.contains(flag);
    }

    public int getExpiry(){
        return expiry;
    }

    public void setExpiry(int expiry){
        this.expiry=expiry;
    }

    public void setStateDirty(){
        checkFrozen();
        if(docState.equals(DocumentState.SYNC)){
            docState = DocumentState.DIRTY;
        }
    }

    public void setStateDeleted(){
        checkFrozen();
        docState = DocumentState.DELETED;
    }

    public void setStateSync(){
        docState = DocumentState.SYNC;
    }

    public DocumentState getState(){
        return docState;
    }

    public long getVbucketID() {
        return vbucketID;
    }

    public void setVbucketID(long vbucketID) {
        this.vbucketID = vbucketID;
    }

    public long getVbucketUUID() {
        return vbucketUUID;
    }

    public void setVbucketUUID(long vbucketUUID) {
        this.vbucketUUID = vbucketUUID;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String toString(){
        return
                "key   : "+key+",\n"+
                "cas   : "+cas+",\n"+
                "mut   : "+vbucketID+"#"+vbucketUUID+"#"+sequenceNumber+",\n"+
                "lock  : "+isLocked+",\n"+
                "size  : "+dbDocSize+",\n"+
                "state : "+docState +",\n"+
                "flags : "+flags.toString();
    }

    public void freeze(){
        this.isFrozen = true;
    }

    public void unfreeze(){
        this.isFrozen = false;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    private void checkFrozen(){
        if(isFrozen){
            throw new IllegalStateException("The modification on the document isn't allowed");
        }
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setDbData(byte[] dbData) {
        this.dbData = dbData;
    }

    public byte[] getDbData() {
        return dbData;
    }

    public long getUpdatedFromCas() {
        return updatedFromCas;
    }

    public void setUpdatedFromCas(long updatedFromCas) {
        this.updatedFromCas = updatedFromCas;
    }
}
