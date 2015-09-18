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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/08/2015.
 */
public class DaemonLifeCycle implements IDaemonLifeCycle {
    private final AbstractDaemon _daemon;
    private final List<Listener>  _listeners = new ArrayList<>();
    private Status _status = Status.STOPPED;
    
    public DaemonLifeCycle(AbstractDaemon daemon){
        _daemon = daemon;
    }

    @Override
    synchronized public void start() throws Exception {
        if(_status== Status.STOPPED||
                _status== Status.STARTING) {
            _status = Status.STARTING;
            for (Listener listener : _listeners) {
                listener.lifeCycleStarting(this);
            }
        }
        if(_status== Status.STARTING||
                _status== Status.HALTED) {
            for (Listener listener : _listeners) {
                listener.lifeCycleStarted(this);
            }
            _status = Status.STARTED;
        }
    }

    @Override
    synchronized public void halt() throws Exception {
        if(_status== Status.STARTED) {
            for (Listener listener : _listeners) {
                listener.lifeCycleHalt(this);
            }
            _status = Status.HALTED;
        }
    }


    @Override
    synchronized public void stop() throws Exception {
        if(_status== Status.STARTED) {
            _status = Status.STOPPING;
            for (Listener listener : _listeners) {
                listener.lifeCycleStopping(this);
            }
        }
        if(_status== Status.HALTED ||
                _status == Status.STOPPING){
            for (Listener listener : _listeners) {
                listener.lifeCycleStopped(this);
            }
            _status = Status.STOPPED;
        }

        this.notifyAll();
    }

    @Override
    synchronized public void reload() throws Exception {
        if(_status== Status.STARTED){
            for (Listener listener : _listeners) {
                listener.lifeCycleReload(this);
            }
        }
    }

    @Override
    synchronized public void join() throws Exception{
        while(!_status.equals(Status.STOPPED)) {
            this.wait();
        }
    }

    @Override
    synchronized public void join(long timeout) throws Exception{
        while(!_status.equals(Status.STOPPED)) {
            this.wait(timeout);
        }
    }


    @Override
    synchronized public boolean isRunning() {
        return _status== Status.STARTED; //todo distinguish from started
    }

    @Override
    synchronized public boolean isHalt() {
        return _status== Status.HALTED;
    }

    @Override
    synchronized public boolean isStarted() {
        return _status== Status.STARTED;
    }

    @Override
    synchronized public boolean isStarting() {
        return _status== Status.STARTING;
    }

    @Override
    synchronized public boolean isStopping() {
        return _status== Status.STOPPING;
    }

    @Override
    synchronized public boolean isStopped() {
        return _status== Status.STOPPED;
    }

    @Override
    synchronized public boolean isFailed() {
        return false;  //TODO
    }

    @Override
    synchronized public Status getStatus() {
        return _status;
    }

    @Override
    synchronized public void addLifeCycleListener(DaemonLifeCycle.Listener listener) {
        _listeners.add(listener);
    }

    @Override
    synchronized public void removeLifeCycleListener(DaemonLifeCycle.Listener listener) {
        _listeners.remove(listener);
    }

    @Override
    public AbstractDaemon getDaemon() {
        return _daemon;
    }
}
