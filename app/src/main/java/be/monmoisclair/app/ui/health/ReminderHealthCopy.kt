package be.monmoisclair.app.ui.health

import be.monmoisclair.app.data.reminder.ReminderHealthReport
import be.monmoisclair.app.ui.components.CarnetBannerKind

/** Résumé actionnable — écran Alertes (seniors). */
object ReminderHealthCopy {

    /**
     * Bandeau en tête si quelque chose peut empêcher les rappels ou la copie.
     * Null = rien à signaler (ou tout vert).
     */
    fun topBanner(report: ReminderHealthReport): String? {
        if (report.isFullyHealthy) return null
        if (report.remindersBlocked) {
            val parts = buildList {
                if (!report.notificationsGranted) add("autoriser les notifications")
                if (!report.exactAlarmGranted) add("autoriser le réveil à l’heure")
            }
            if (parts.isNotEmpty()) {
                return "Pour les rappels du matin : ${parts.joinToString(" et ")}."
            }
        }
        if (!report.batteryOptimizationIgnored) {
            return "Batterie limitée : les alertes peuvent arriver en retard — touchez le bouton ci-dessous."
        }
        if (!report.safBackupOk) {
            return "Pas de copie à jour dans Documents — voir Réglages → Sauvegarde."
        }
        return null
    }

    /** Contraste sémantique : rouge = rappels bloqués ; orange = batterie / copie. */
    fun topBannerKind(report: ReminderHealthReport): CarnetBannerKind {
        if (report.isFullyHealthy) return CarnetBannerKind.Ok
        return if (report.remindersBlocked) CarnetBannerKind.Error else CarnetBannerKind.Warn
    }
}
