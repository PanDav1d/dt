package de.doctag.docsrv.ui.forms.system

import de.doctag.docsrv.i18n
import de.doctag.docsrv.i18nText
import de.doctag.docsrv.model.*
import de.doctag.docsrv.propertyOrDefault
import de.doctag.docsrv.propertyOrDefault2
import de.doctag.docsrv.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.property

fun ElementCreator<*>.tagForm(tag: Tag, onSaveClick: (tag: Tag)->Unit) {
    val tag = KVar(tag)

    tag.value.let {
        if(it.style == null)
            it.style = TagStyle()
        if(it.options==null)
            it.options = TagOptions()
        if(it.options?.appendRules==null)
            it.options?.appendRules = TagAppendRules()
    }

    formControl { formCtrl ->
        formInput(i18n("ui.forms.system.tagForm.nameLabel","Name"), "Name", true, tag.propertyOrDefault(Tag::name, ""))
            .with(formCtrl)
            .validate {
                when {
                    it.isNullOrBlank() -> i18n("ui.forms.system.tagForm.nameRequiredMessage","Bitte geben Sie einen Namen für das Tag an")
                    else -> null
                }
            }

        formInput(i18n("ui.forms.system.tagForm.tagDescription","Beschreibung"), i18n("ui.forms.system.tagForm.tagDescriptionInputPlaceholderText","Beschreibung"), true, tag.propertyOrDefault(Tag::description, ""))
            .with(formCtrl)
            .validate {
                when {
                    it.isNullOrBlank() -> i18n("ui.forms.system.tagForm.descriptionRequiredMessage","Bitte geben Sie eine Beschreibung für das Tag an")
                    else -> null
                }
            }

        div(fomantic.ui.field).new {
            label().i18nText("ui.forms.system.tagForm.colorLabel","Farbe des Tags")
            namedColorPicker(tag.property(Tag::style).propertyOrDefault2(TagStyle::backgroundColor, ""))
        }


        div(fomantic.ui.fluid.styled.accordion).new {
            div(fomantic.title).i18nText("ui.forms.system.tagForm.alwaysAttachTitle","Automatisch anfügen").new {
                i(fomantic.icon.dropdown)
            }
            div(fomantic.content).new {
                formInput(i18n("ui.forms.system.tagForm.autoAttachWhenTextContainsLabel","Wenn das Dokument den Text enthält"), "ABC", false, tag.propertyOrDefault(Tag::options, TagOptions()).propertyOrDefault(TagOptions::appendRules, TagAppendRules()).propertyOrDefault(TagAppendRules::whenDocumentContains, ""))
                    .with(formCtrl)
            }
        }
        GlobalScope.launch {
            delay(250)
            browser.execute("""
                ${'$'}('.ui.accordion')
                  .accordion()
                ;
            """.trimIndent())
        }


        div(fomantic.ui.divider.hidden)

        displayErrorMessages(formCtrl)

        formSubmitButton(formCtrl){
            onSaveClick(tag.value)
        }
    }
}

fun ElementCreator<*>.addTagDropdown(exclude: List<AttachedTag>?, onAdd: (Tag)->Unit){
    val tags = db().tags.find().toList().filter { !(exclude?: listOf()).map { it._id }.contains(it._id) }

    if(tags.isNotEmpty()){
        div(fomantic.ui.simple.dropdown.item).new {
            i(fomantic.add.icon)
            div(fomantic.menu).new {
                tags.forEach { tag->
                    div(fomantic.item).apply {
                        on.click { onAdd(tag) }
                    }.new {
                        tag(tag, size = FomanticUiSize.Mini)
                    }
                }
            }
        }
    }
}
