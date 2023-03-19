package jr.brian.issaaiapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jr.brian.issaaiapp.model.local.AppDatabase
import jr.brian.issaaiapp.model.local.ChatsDao
import jr.brian.issaaiapp.model.repository.RepoImpl
import jr.brian.issaaiapp.model.repository.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Provides
//    @Singleton
//    fun provideAPI(): ApiService {
//
//        return ApiService::class.java
//    }

    @Provides
    @Singleton
    fun provideRepository(): Repository = RepoImpl()

    @Provides
    @Singleton
    fun provideDao(appDatabase: AppDatabase): ChatsDao = appDatabase.dao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "chats"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}