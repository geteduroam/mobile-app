package app.eduroam.geteduroam.di.module

import android.content.Context
import app.eduroam.geteduroam.BuildConfig
import app.eduroam.geteduroam.di.api.GetEduroamApi
import app.eduroam.geteduroam.di.api.response.ApiResponseAdapterFactory
import app.eduroam.geteduroam.di.assist.AuthenticationAssistant
import app.eduroam.geteduroam.di.repository.StorageRepository
import com.squareup.moshi.Moshi
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object EduroamModule {

    @Provides
    @Singleton
    internal fun provideApi(retrofit: Retrofit): GetEduroamApi =
        retrofit.create(GetEduroamApi::class.java)

    @Provides
    @Singleton
    internal fun providesStorageRepository(
        @ApplicationContext context: Context,
    ) = StorageRepository(context)

    @Provides
    fun providesOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    internal fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    internal fun providesAuthenticationAssist(
    ) = AuthenticationAssistant()

    @Provides
    @Singleton
    internal fun provideEduroamRetrofit(
        client: Lazy<OkHttpClient>, moshi: Moshi,
    ): Retrofit {
        return Retrofit.Builder().callFactory { client.get().newCall(it) }
            .addCallAdapterFactory(ApiResponseAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BuildConfig.DISCOVERY_BASE_URL).build()
    }

    @Provides
    @Singleton
    internal fun provideMoshi(): Moshi = Moshi.Builder().build()
}