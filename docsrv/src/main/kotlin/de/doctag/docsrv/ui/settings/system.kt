package de.doctag.docsrv.ui.settings

import de.doctag.docsrv.model.*
import de.doctag.docsrv.ui.ToastKind
import de.doctag.docsrv.ui.active
import de.doctag.docsrv.ui.forms.system.designForm
import de.doctag.docsrv.ui.forms.system.hostnameEditForm
import de.doctag.docsrv.ui.forms.system.mailSettingsEditForm
import de.doctag.docsrv.ui.forms.system.workflowListEditForm
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
            pageBorderAndTitle("Einstellungen") { pageArea ->

                div(fomantic.content).new() {
                    div(fomantic.ui.grid).new {
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

                                            pageArea.showToast("Hostname geändert", ToastKind.Success)
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

                                            pageArea.showToast("E-Mail Einstellungen geändert", ToastKind.Success)
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
                                        pageArea.showToast("Design Einstellungen geändert", ToastKind.Success)
                                    }
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
    DESIGN
}

fun ElementCreator<*>.systemSettingsMenu(activeItem: SystemSettingsActiveItem) {
    div(fomantic.ui.secondary.vertical.menu).new {
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.HOST), "/settings/system/host").text("Hostname")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.MAIL), "/settings/system/mail").text("E-Mail")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.WORKFLOW), "/settings/system/workflow").text("Workflows")
        a(fomantic.ui.item.active(activeItem == SystemSettingsActiveItem.DESIGN), "/settings/system/design").text("Design")
    }
}