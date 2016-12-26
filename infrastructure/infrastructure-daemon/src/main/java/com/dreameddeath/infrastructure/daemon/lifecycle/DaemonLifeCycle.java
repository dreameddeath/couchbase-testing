/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.infrastructure.daemon.lifecycle;


import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/08/2015.
 */
public class DaemonLifeCycle implements IDaemonLifeCycle {
    private static final Logger LOG = LoggerFactory.getLogger(DaemonLifeCycle.class);
    private final AbstractDaemon daemon;
    private final List<Listener>  listeners = new ArrayList<>();
    private Status status = Status.STOPPED;
    private DateTime creationDate;
    private DateTime lastStartDate;
    private DateTime lastHaltStartDate;


    public DaemonLifeCycle(AbstractDaemon daemon){
        this.daemon = daemon;
        creationDate = new DateTime();
    }

    synchronized private void manageException(Exception e,String action) throws Exception{
        LOG.error("Error during "+action,e);
        status=Status.FAILED;
        for(Listener listener:listeners){
            listener.lifeCycleFailure(this,e);
        }
        this.notifyAll();
        throw e;
    }

    private void sortListener(boolean reverse){
        final int coef = (reverse)?-1:1;
        listeners.sort((l1, l2) -> coef * Integer.compare(l1.getRank(), l2.getRank()));
    }

    @Override
    synchronized public void start() throws Exception {
        sortListener(false);
        try {
            if (status == Status.STOPPED ||
                    status == Status.STARTING) {
                status = Status.STARTING;
                for (Listener listener : listeners) {
                    listener.lifeCycleStarting(this);
                }
            }
            if (status == Status.STARTING ||
                    status == Status.HALTED) {
                for (Listener listener : listeners) {
                    listener.lifeCycleStarted(this);
                }
                status = Status.STARTED;
            }
            lastStartDate = new DateTime();
        }
        catch(Exception e){
            manageException(e,"start");
        }
    }

    @Override
    synchronized public void halt() throws Exception {
        try {
            if (status == Status.STARTED) {
                sortListener(true);
                for (Listener listener : listeners) {
                    listener.lifeCycleHalt(this);
                }
            }
            else if(status==Status.STOPPED){
                sortListener(false);
                for(Listener listener:listeners){
                    listener.lifeCycleStarting(this);
                }
            }
            status = Status.HALTED;
            lastHaltStartDate = new DateTime();
        }
        catch(Exception e){
            manageException(e, "halt");
        }
    }


    @Override
    synchronized public void stop() throws Exception {
        sortListener(true);
        try {
            if (status == Status.STARTED) {
                status = Status.STOPPING;
                for (Listener listener : listeners) {
                    try {
                        listener.lifeCycleStopping(this);
                    }
                    catch(Throwable e){
                        LOG.error("The listener "+listener.toString()+ " raised an error during stopping",e);
                    }
                }
            }
            if (status == Status.HALTED ||
                    status == Status.STOPPING) {
                for (Listener listener : listeners) {
                    listener.lifeCycleStopped(this);
                }
                status = Status.STOPPED;
            }
            this.notifyAll();
        }
        catch(Exception e){
            manageException(e,"stop");
        }
    }


    @Override
    synchronized public void join() throws Exception{
        while(!(isStopped() || isFailed())) {
            this.wait();
        }
    }

    @Override
    synchronized public void join(long timeout) throws Exception{
        while(!(isStopped() || isFailed())) {
            this.wait(timeout);
        }
    }


    @Override
    synchronized public boolean isRunning() {
        return status== Status.STARTED; //todo distinguish from started
    }

    @Override
    synchronized public boolean isHalt() {
        return status== Status.HALTED;
    }

    @Override
    synchronized public boolean isStarted() {
        return status== Status.STARTED;
    }

    @Override
    synchronized public boolean isStarting() {
        return status== Status.STARTING;
    }

    @Override
    synchronized public boolean isStopping() {
        return status== Status.STOPPING;
    }

    @Override
    synchronized public boolean isStopped() {
        return status== Status.STOPPED;
    }

    @Override
    synchronized public boolean isFailed() {
        return status==Status.FAILED;
    }

    @Override
    synchronized public void addLifeCycleListener(DaemonLifeCycle.Listener listener) {
        listeners.add(listener);
    }

    @Override
    synchronized public void removeLifeCycleListener(DaemonLifeCycle.Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public AbstractDaemon getDaemon() {
        return daemon;
    }

    @Override
    synchronized public Status getStatus() {
        return status;
    }

    @Override
    public DateTime getCreationDate() {
        return creationDate;
    }

    @Override
    public DateTime getLastStartDate() {
        return lastStartDate;
    }

    @Override
    public DateTime getLastHaltStartDate() {
        return lastHaltStartDate;
    }
}
