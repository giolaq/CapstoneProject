package com.laquysoft.motivetto.components;

import com.laquysoft.motivetto.common.MainThreadBus;
import com.laquysoft.motivetto.modules.EventBusModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by joaobiriba on 30/01/16.
 */
@Singleton
@Component(modules = {EventBusModule.class})
public interface EventBusComponent {
    MainThreadBus provideMainThreadBus();
}