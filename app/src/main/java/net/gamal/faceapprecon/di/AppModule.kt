package net.gamal.faceapprecon.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.gamal.faceapprecon.detection.data.repository.localDs.EncodedFaceLocalDS
import net.gamal.faceapprecon.detection.datasource.dao.EncodedFacesDAO
import net.gamal.faceapprecon.detection.datasource.db.EncodedFacesDatabase
import net.gamal.faceapprecon.detection.domain.repository.localDs.IEncodedFaceLocalDS
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideFacesDB(@ApplicationContext context: Context): EncodedFacesDatabase =
        Room.databaseBuilder(context, EncodedFacesDatabase::class.java, "faces_db")
            .fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideFacesDBDao(encodedDB: EncodedFacesDatabase): EncodedFacesDAO =
        encodedDB.encodedFaceDao()

    @Singleton
    @Provides
    fun provideEncodedFacesDAO(dao: EncodedFacesDAO): IEncodedFaceLocalDS = EncodedFaceLocalDS(dao)

}