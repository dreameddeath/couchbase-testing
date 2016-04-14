package com.dreameddeath.testing.dataset.model;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetRange {
    private Integer min=null;
    private Integer max=null;
    private Integer exact=null;

    public void setMin(Integer min) {
        this.min = min;
    }

    public void setMin(String min){
        this.setMin(Integer.parseInt(min));
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public void setMax(String max) {
        this.setMax(Integer.parseInt(max));
    }

    public void setExact(Integer exact) {
        this.exact = exact;
    }

    public void setExact(String exact) {
        this.setExact(Integer.parseInt(exact));
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if(this.exact!=null){
            sb.append(exact);
        }
        else if(min==null && max==null){
            sb.append("*");
        }
        else{
            sb.append(min!=null?min:0);
            sb.append("..");
            sb.append(max!=null?max:"$");

        }
        sb.append("]");
        return sb.toString();
    }
}
