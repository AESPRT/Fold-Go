package com.aesprt.foldgo.di

import androidx.room.Room
import com.aesprt.foldgo.data.local.FoldGoDatabase
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.data.repository.InventoryRepositoryImpl
import com.aesprt.foldgo.data.repository.MachineRepositoryImpl
import com.aesprt.foldgo.data.repository.OrderRepositoryImpl
import com.aesprt.foldgo.data.repository.ShopRepositoryImpl
import com.aesprt.foldgo.domain.repository.InventoryRepository
import com.aesprt.foldgo.domain.repository.MachineRepository
import com.aesprt.foldgo.domain.repository.OrderRepository
import com.aesprt.foldgo.domain.repository.ShopRepository
import com.aesprt.foldgo.domain.usecase.GetActiveOrdersUseCase
import com.aesprt.foldgo.presentation.dashboard.DashboardViewModel
import com.aesprt.foldgo.presentation.history.HistoryViewModel
import com.aesprt.foldgo.presentation.machines.MachineViewModel
import com.aesprt.foldgo.presentation.onboarding.OnboardingViewModel
import com.aesprt.foldgo.presentation.order.OrderDetailViewModel
import com.aesprt.foldgo.presentation.order.OrderEntryViewModel
import com.aesprt.foldgo.presentation.shop.ShopRegistrationViewModel
import com.aesprt.foldgo.presentation.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { PreferenceManager(androidContext()) }
    single<FoldGoDatabase> {
        Room.databaseBuilder(
            androidContext(),
            FoldGoDatabase::class.java,
            "foldgo_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }
    single { get<FoldGoDatabase>().shopDao }
    single { get<FoldGoDatabase>().orderDao }
    single { get<FoldGoDatabase>().machineDao }
    single { get<FoldGoDatabase>().inventoryDao }
    
    single<ShopRepository> { ShopRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<MachineRepository> { MachineRepositoryImpl(get()) }
    single<InventoryRepository> { InventoryRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetActiveOrdersUseCase(get()) }
}

val presentationModule = module {
    viewModel { SplashViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { OrderEntryViewModel(get(), get()) }
    viewModel { (orderId: String) -> OrderDetailViewModel(orderId, get(), get()) }
    viewModel { MachineViewModel(get(), get(), get()) }
    viewModel { ShopRegistrationViewModel(get(), get()) }
}

val appModule = module {
    includes(dataModule, domainModule, presentationModule)
}
