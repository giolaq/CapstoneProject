package com.laquysoft.motivetto.modules;

import com.laquysoft.motivetto.common.MainThreadBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by joaobiriba on 30/01/16.
 */
@Module
public class EventBusModule {

    @Provides @Singleton
    MainThreadBus provideBus() {
        return MainThreadBus.getInstance();
    }

}