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

    public DaemonLifeCycle(AbstractDaemon daemon){
        _daemon = daemon;
    }

    @Override
    synchronized public void start() throws Exception {
        if(_daemon.getStatus()== AbstractDaemon.Status.STOPPED||
                _daemon.getStatus()== AbstractDaemon.Status.STARTING) {
            _daemon.setStatus(AbstractDaemon.Status.STARTING);
            for (Listener listener : _listeners) {
                listener.lifeCycleStarting(this);
            }
        }
        if(_daemon.getStatus()== AbstractDaemon.Status.STARTING||
                _daemon.getStatus()== AbstractDaemon.Status.HALTED) {
            for (Listener listener : _listeners) {
                listener.lifeCycleStarted(this);
            }
            _daemon.setStatus(AbstractDaemon.Status.STARTED);
        }
    }

    @Override
    public void halt() throws Exception {
        if(_daemon.getStatus()== AbstractDaemon.Status.STARTED) {
            for (Listener listener : _listeners) {
                listener.lifeCycleHalt(this);
            }
            _daemon.setStatus(AbstractDaemon.Status.HALTED);
        }
    }


    @Override
    public void stop() throws Exception {
        if(_daemon.getStatus()== AbstractDaemon.Status.STARTED) {
            _daemon.setStatus(AbstractDaemon.Status.STOPPING);
            for (Listener listener : _listeners) {
                listener.lifeCycleStopping(this);
            }
        }
        if(_daemon.getStatus()== AbstractDaemon.Status.HALTED ||
                _daemon.getStatus() == AbstractDaemon.Status.STOPPING){
            for (Listener listener : _listeners) {
                listener.lifeCycleStopped(this);
            }
            _daemon.setStatus(AbstractDaemon.Status.STOPPED);
        }
    }

    @Override
    synchronized public void reload() throws Exception {
        if(_daemon.getStatus()== AbstractDaemon.Status.STARTED){
            for (Listener listener : _listeners) {
                listener.lifeCycleReload(this);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return _daemon.getStatus()== AbstractDaemon.Status.STARTED; //todo distinguish from started
    }

    @Override
    public boolean isHalt() {
        return _daemon.getStatus()== AbstractDaemon.Status.HALTED;
    }

    @Override
    public boolean isStarted() {
        return _daemon.getStatus()== AbstractDaemon.Status.STARTED;
    }

    @Override
    public boolean isStarting() {
        return _daemon.getStatus()== AbstractDaemon.Status.STARTING;
    }

    @Override
    public boolean isStopping() {
        return _daemon.getStatus()== AbstractDaemon.Status.STOPPING;
    }

    @Override
    public boolean isStopped() {
        return _daemon.getStatus()== AbstractDaemon.Status.STOPPED;
    }

    @Override
    public boolean isFailed() {
        return false;  //TODO
    }

    @Override
    public void addLifeCycleListener(DaemonLifeCycle.Listener listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeLifeCycleListener(DaemonLifeCycle.Listener listener) {
        _listeners.remove(listener);
    }

    @Override
    public AbstractDaemon getDaemon() {
        return _daemon;
    }
}
