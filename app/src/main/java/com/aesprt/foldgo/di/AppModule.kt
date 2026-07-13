package com.aesprt.foldgo.di

import androidx.room.Room
import androidx.work.WorkManager
import com.aesprt.foldgo.core.notification.NotificationHelper
import com.aesprt.foldgo.data.local.FoldGoDatabase
import com.aesprt.foldgo.data.local.PreferenceManager
import com.aesprt.foldgo.data.remote.SmsService
import com.aesprt.foldgo.data.repository.*
import com.aesprt.foldgo.domain.repository.*
import com.aesprt.foldgo.domain.usecase.*
import com.aesprt.foldgo.presentation.auth.LoginViewModel
import com.aesprt.foldgo.presentation.auth.StaffSelectionViewModel
import com.aesprt.foldgo.presentation.dashboard.DashboardViewModel
import com.aesprt.foldgo.presentation.history.HistoryViewModel
import com.aesprt.foldgo.presentation.machines.MachineViewModel
import com.aesprt.foldgo.presentation.onboarding.OnboardingViewModel
import com.aesprt.foldgo.presentation.order.OrderDetailViewModel
import com.aesprt.foldgo.presentation.order.OrderEntryViewModel
import com.aesprt.foldgo.presentation.settings.PreferencesViewModel
import com.aesprt.foldgo.presentation.settings.SettingsViewModel
import com.aesprt.foldgo.presentation.shop.ShopInfoViewModel
import com.aesprt.foldgo.presentation.shop.ShopRegistrationViewModel
import com.aesprt.foldgo.presentation.services.ServicesViewModel
import com.aesprt.foldgo.presentation.splash.SplashViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import org.koin.core.qualifier.named

val dataModule = module {
    single { PreferenceManager(androidContext(), get()) }
    single { NotificationHelper(androidContext(), get()) }
    single { WorkManager.getInstance(androidContext()) }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Retrofit for Semaphore (Original)
    single(named("SemaphoreRetrofit")) {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://api.semaphore.co/")
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // Retrofit for HttpSMS (New)
    /*single(named("HttpSmsRetrofit")) {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("https://api.httpsms.com/v1/")
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }*/

    single<SmsService> { get<Retrofit>(named("SemaphoreRetrofit")).create(SmsService::class.java) }
    /*single<HttpSmsService> { get<Retrofit>(named("HttpSmsRetrofit")).create(HttpSmsService::class.java) }*/

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
    single { get<FoldGoDatabase>().staffDao }
    single { get<FoldGoDatabase>().machineCategoryDao }
    single { get<FoldGoDatabase>().serviceDao }
    single { get<FoldGoDatabase>().orderBatchDao }

    single<ShopRepository> { ShopRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<MachineRepository> { MachineRepositoryImpl(get(), get(), get()) }
    single<StaffRepository> { StaffRepositoryImpl(get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }

    // Switch between Semaphore and HttpSMS here
    // Currently set to HttpSMS
    /*single<SmsRepository> { HttpSmsRepositoryImpl(get()) }*/
    single<SmsRepository> { SmsRepositoryImpl(get()) } // Semaphore implementation

    single<OrderBatchRepository> { OrderBatchRepositoryImpl(get()) }
}

val domainModule = module {
    factory { GetActiveOrdersUseCase(get()) }

    // Machine UseCases
    factory { GetMachinesUseCase(get()) }
    factory { AddMachineUseCase(get()) }
    factory { GetMachineCategoriesUseCase(get()) }
    factory { AddMachineCategoryUseCase(get()) }
    factory { UpdateMachineStatusUseCase(get()) }
    factory { StartMachineCycleUseCase(get()) }
    factory { FinishMachineCycleUseCase(get()) }

    // Order & Service UseCases
    factory { GetAllOrdersUseCase(get()) }
    factory { GetOrderByIdUseCase(get()) }
    factory { UpsertOrderUseCase(get()) }
    factory { DeleteOrderUseCase(get()) }
    factory { GetServicesUseCase(get()) }
    factory { UpsertServiceUseCase(get()) }
    factory { SendSmsUseCase(get()) }

    // Batch UseCases
    factory { GetBatchesByOrderIdUseCase(get()) }
    factory { GetActiveBatchByMachineIdUseCase(get()) }
    factory { UpsertOrderBatchUseCase(get()) }
    factory { DeleteOrderBatchUseCase(get()) }

    // Staff UseCases
    factory { GetStaffByShopUseCase(get()) }
    factory { GetStaffByIdUseCase(get()) }
    factory { UpsertStaffUseCase(get()) }
    factory { DeleteStaffUseCase(get()) }

    // Shop UseCases
    factory { GetShopByIdUseCase(get()) }
    factory { UpsertShopUseCase(get()) }
    factory { HasShopUseCase(get()) }
}

val presentationModule = module {
    viewModel { SplashViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { OrderEntryViewModel(get(), get(), get(), get()) }
    viewModel { (orderId: String) ->
        OrderDetailViewModel(
            orderId,
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        MachineViewModel(
            get(), get(), get(), get(), get(), get(), get(), get(), get(),
            get(), get(), get(), get()
        )
    }
    viewModel { ShopRegistrationViewModel(get(), get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { StaffSelectionViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { ShopInfoViewModel(get(), get()) }
    viewModel { ServicesViewModel(get(), get()) }
    viewModel { PreferencesViewModel(get()) }
}

val appModule = module {
    includes(dataModule, domainModule, presentationModule)
}
