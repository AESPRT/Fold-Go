package com.aesprt.foldgo.di

import androidx.room.Room
import com.aesprt.foldgo.data.local.FoldGoDatabase
import com.aesprt.foldgo.data.repository.OrderRepositoryImpl
import com.aesprt.foldgo.domain.repository.OrderRepository
import com.aesprt.foldgo.domain.usecase.GetActiveOrdersUseCase
import com.aesprt.foldgo.presentation.dashboard.DashboardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            FoldGoDatabase::class.java,
            "foldgo_db"
        ).build()
    }
    single { get<FoldGoDatabase>().orderDao }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetActiveOrdersUseCase(get()) }
}

val presentationModule = module {
    viewModel { DashboardViewModel(get()) }
}

val appModule = module {
    includes(dataModule, domainModule, presentationModule)
}
