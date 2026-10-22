package be.monmoisclair.app.data.widget

fun interface CarnetWidgetSink {
    suspend fun refresh()

    companion object {
        val Noop = CarnetWidgetSink { }
    }
}
