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

package com.dreameddeath.infrastructure.daemon.lifecycle;


import com.dreameddeath.infrastructure.daemon.AbstractDaemon;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/08/2015.
 */
public class DaemonLifeCycle implements IDaemonLifeCycle {
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

    @Override
    synchronized public void start() throws Exception {
        if(status== Status.STOPPED||
                status== Status.STARTING) {
            status = Status.STARTING;
            for (Listener listener : listeners) {
                listener.lifeCycleStarting(this);
            }
        }
        if(status== Status.STARTING||
                status== Status.HALTED) {
            for (Listener listener : listeners) {
                listener.lifeCycleStarted(this);
            }
            status = Status.STARTED;
        }
        lastStartDate = new DateTime();
    }

    @Override
    synchronized public void halt() throws Exception {
        if(status== Status.STARTED) {
            for (Listener listener : listeners) {
                listener.lifeCycleHalt(this);
            }
            status = Status.HALTED;
            lastHaltStartDate = new DateTime();
        }
    }


    @Override
    synchronized public void stop() throws Exception {
        if(status== Status.STARTED) {
            status = Status.STOPPING;
            for (Listener listener : listeners) {
                listener.lifeCycleStopping(this);
            }
        }
        if(status== Status.HALTED ||
                status == Status.STOPPING){
            for (Listener listener : listeners) {
                listener.lifeCycleStopped(this);
            }
            status = Status.STOPPED;
        }

        this.notifyAll();
    }

    @Override
    synchronized public void reload() throws Exception {
        if(status== Status.STARTED){
            for (Listener listener : listeners) {
                listener.lifeCycleReload(this);
            }
        }
    }

    @Override
    synchronized public void join() throws Exception{
        while(!status.equals(Status.STOPPED)) {
            this.wait();
        }
    }

    @Override
    synchronized public void join(long timeout) throws Exception{
        while(!status.equals(Status.STOPPED)) {
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
        return false;  //TODO
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
