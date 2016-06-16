package com.dreameddeath.core.notification.dispatch;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.notification.model.v1.Event;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 24/05/2016.
 */
public interface IDispatcher {
    <T extends Event> Observable<DispatchResult> asyncDispatch(Observable<T> event, ICouchbaseSession session);
}
