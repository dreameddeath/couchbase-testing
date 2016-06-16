package com.dreameddeath.core.notification.dispatch;

import com.dreameddeath.core.notification.listener.SubmissionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 29/05/2016.
 */
public class DispatchResult {
    List<SubmissionResult> submissionResultList=new ArrayList<>();
    private boolean hasErrors=false;

    synchronized public DispatchResult addResult(SubmissionResult result){
        submissionResultList.add(result);
        hasErrors|=result.isFailure();
        return this;
    }

    public boolean isSuccess() {
        return !hasErrors;
    }

    public boolean hasFailures(){
        return hasErrors;
    }

    public List<SubmissionResult> getFailedSubmissions(){
        return submissionResultList.stream().filter(SubmissionResult::isFailure).collect(Collectors.toList());
    }
}
