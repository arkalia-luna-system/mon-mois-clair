package be.monmoisclair.app.di

import be.monmoisclair.app.data.widget.CarnetWidgetRefresher
import be.monmoisclair.app.data.widget.CarnetWidgetSink
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    @Singleton
    abstract fun bindCarnetWidgetSink(impl: CarnetWidgetRefresher): CarnetWidgetSink
}
