package com.kabi.run.data.di

import com.kabi.core.domain.run.SyncRunScheduler
import com.kabi.run.data.CreateRunWorker
import com.kabi.run.data.DeleteRunWorker
import com.kabi.run.data.FetchRunWorker
import com.kabi.run.data.SyncRunWorkerScheduler
import com.kabi.run.data.connectivity.PhoneToWatchConnector
import com.kabi.run.domain.WatchConnector
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val runDataModule = module {
    workerOf(::CreateRunWorker)
    workerOf(::FetchRunWorker)
    workerOf(::DeleteRunWorker)

    singleOf(::SyncRunWorkerScheduler).bind<SyncRunScheduler>()
    singleOf(::PhoneToWatchConnector).bind<WatchConnector>()
}