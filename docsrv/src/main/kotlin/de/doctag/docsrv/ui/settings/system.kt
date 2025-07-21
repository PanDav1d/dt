package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.active
import de.doctag.docsrv.ui.forms.system.*
import de.doctag.docsrv.ui.pageBorderAndTitle
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import org.litote.kmongo.save

fun ElementCreator<*>.handleSystemSettings(subPage : KVar<String>) {

    render(subPage, container = {div()}) { rSubPage ->

        val activeItem = SystemSettingsActiveItem.valueOf(rSubPage.toUpperCase())

        authRequired {
            pageBorderAndTitle(i18n("ui.settings.system.pageTitle","Einstellungen")) { pageArea ->

                div(fomantic.content).new() {
                    div(fomantic.ui.stackable.grid).new {
                        div(fomantic.ui.sixteen.wide.column).new {
                            settingsTabMenu(SettingsTabMenuActiveItem.System) {}
                            div(fomantic.ui.divider.hidden)
                        }

                        div(fomantic.ui.three.wide.column).new {
                            systemSettingsMenu(activeItem)
                        }
                        div(fomantic.ui.thirteen.wide.column).new {
                            when(activeItem){
                                SystemSettingsActiveItem.HOST->{
                                    hostnameEditForm(db().currentConfig.hostname){newHostName->
                                        db().currentConfig.let { conf ->
                                            conf.hostname = newHostName
                                            db().config.save(conf)

                                            pageArea.showToast(i18n("ui.settings.keys.hostnameChangedMessage","Hostname geändert"), ToastKind.Success)
                                        }
                                    }
                                }

                                SystemSettingsActiveItem.MAIL->{
                                    mailSettingsEditForm(
                                            db().currentConfig.outboundMail?: OutboundMailConfig(), 
                                            db().currentConfig?.inboundMail?: InboundMailConfig()){ outbound, inbound ->

                                        db().currentConfig.let { conf ->
                                            conf.outboundMail = outbound
                                            conf.inboundMail = inbound
                                            db().config.save(conf)

                                            pageArea.showToast(i18n("ui.settings.keys.mailSettingsChanged","E-Mail-Einstellungen geändert"), ToastKind.Success)
                                        }
                                    }
                                }

                                SystemSettingsActiveItem.WORKFLOW -> {
                                    workflowListEditForm(pageArea)
                                }

                                SystemSettingsActiveItem.DESIGN->{
                                    designForm(db().currentConfig.design ?: DesignConfig()){designConfig ->
                                        db().currentConfig.let { conf->
                                            conf.design = designConfig
                                            db().config.save(conf)
                                        }
                                        pageArea.showToast(i18n("ui.settings.keys.designSettingsChanged","Design-Einstellungen geändert"), ToastKind.Success)
                                    }
                                }
                                SystemSettingsActiveItem.SEARCH->{
                                    search_settings_form()
                                }
                                SystemSettingsActiveItem.SECURITY->{
                                    security_settings_form {
                                        db().currentConfig.let { config ->
                                            config.security = it
                                            db().config.save(config)
                                        }
                                    }
                                }
                                SystemSettingsActiveItem.TAGS->{
                                    tag_settings_form(pageArea)
                                }
                                SystemSettingsActiveItem.NOTIFICATIONS -> {
                                    notifications_form(pageArea)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SystemSettingsActiveItem {
    HOST,
    MAIL,
    WORKFLOW,
    DESIGN,
    SEARCH,
    SECURITY,
    TAGS,
    NOTIFICATIONS
}

fun ElementCreator<*>.systemSettingsMenu(activeItem: SystemSettingsActiveItem) {
    div(fomantic.ui.secondary.vertical.menu).new {
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.HOST), "/settings/system/host").i18nText("ui.settings.keys.menu.hostname","Hostname")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.MAIL), "/settings/system/mail").i18nText("ui.settings.keys.menu.mail","E-Mail")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.WORKFLOW), "/settings/system/workflow").i18nText("ui.settings.keys.menu.workflows","Workflows")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.DESIGN), "/settings/system/design").i18nText("ui.settings.keys.menu.design","Design")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.SEARCH), "/settings/system/search").i18nText("ui.settings.keys.menu.search","Suche")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.SECURITY), "/settings/system/security").i18nText("ui.settings.keys.security","Sicherheit")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.TAGS), "/settings/system/tags").i18nText("ui.settings.keys.menu.tags","Tags")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.NOTIFICATIONS), "/settings/system/notifications").i18nText("ui.settings.keys.menu.notifications","Benachrichtigungen")
    }
}