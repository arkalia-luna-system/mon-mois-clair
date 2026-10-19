package be.monmoisclair.app.di

import be.monmoisclair.app.data.biometric.BiometricAuthGateway
import be.monmoisclair.app.data.biometric.BiometricAuthGatewayImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BiometricModule {
    @Binds
    @Singleton
    abstract fun bindBiometricAuthGateway(impl: BiometricAuthGatewayImpl): BiometricAuthGateway
}
