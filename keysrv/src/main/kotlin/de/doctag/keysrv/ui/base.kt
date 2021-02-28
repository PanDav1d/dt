package de.doctag.keysrv.ui

import de.doctag.keysrv.ui.modals.keyGeneratorModal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render

fun WebBrowser.navigateTo(path:String){
    this.evaluate("window.location = \"${path}\"")
}

fun ElementCreator<*>.centeredBox( contentBlock: ElementCreator<DivElement>.()->Unit){

    this.browser.doc.head.new(){
        element("style", mapOf("type" to "text/css")).innerHTML("""
            body {
              background-color: #DADADA;
            }
            #K1 {
              height: 100%;    
            }
            .grid{
              height: 100%;
            }
            .image {
              margin-top: -100px;
            }
            .column {
              max-width: 450px;
            }
        """.trimIndent())
    }

    div(fomantic.ui.middle.aligned.center.aligned.grid).new {

        div(fomantic.column).new(){
            div(fomantic.ui.segment).new(){
                contentBlock(this)
            }
        }
    }
}

enum class ToastKind{
    Success,
    Warning,
    Error
}

class PageArea(val ec: ElementCreator<*>){

    val toastTitle = KVar("")
    val toastKind = KVar(ToastKind.Success)
    val toastVisible = KVar(false)

    fun showToast(title:String, kind:ToastKind){
        toastVisible.value = true
        toastTitle.value = title
        toastKind.value = kind

        GlobalScope.launch {
            delay(5000)
            toastVisible.value=false
        }
    }
}

fun ElementCreator<*>.pageBorderAndTitle(title: String, content: ElementCreator<DivElement>.(page:PageArea) -> Unit) {
    val area = PageArea(this)

    this.browser.doc.head.new(){
        meta(name = "Description", content = "Dokumentenserver für signierte Dokumente")
        element("style", mapOf("type" to "text/css")).innerHTML("""
            .main.container{
                margin-top: 4em;
            }
            .actionIcon{
              color: black;
            }
        """.trimIndent())
    }

    div(fomantic.ui.fixed.menu).new{
        a(fomantic.header.item, href="/").text("KeySrv")
        div(fomantic.right.menu).new{

            render(area.toastVisible) { isVisible ->
                if(isVisible) {
                    div(fomantic.ui.item).new{

                        val class_ = when(area.toastKind.value){
                            ToastKind.Success-> fomantic.green
                            ToastKind.Warning-> fomantic.yellow
                            ToastKind.Error-> fomantic.red
                        }

                        div(class_.ui.label.horizontal).text(area.toastTitle)
                    }
                }
            }

            a(fomantic.item, href="#").apply {
                new {
                    i(fomantic.ui.random.icon)
                }
            }.on.click {
                val modal = keyGeneratorModal()
                modal.open()
            }

            a(fomantic.item, href="/settings/users").new {
                i(fomantic.ui.cog.icon)
            }
            a(fomantic.item,href = "/logout").text("Abmelden")
        }

    }

    div(fomantic.ui.main).apply {
        setAttributeRaw("style", "width: 98vw; margin-left: 1vw;")
    }.new {

        //div(fomantic.column).new {
            div(fomantic.ui.vertical.segment).new {

                h1(fomantic.ui.header).text(title)
                div(fomantic.ui.content).new {
                    content(this,area)
                }

            }
        //}
    }
}

class ModalView(val ec: ElementCreator<*>, val id: String = (modalCounter++).toString(), var isOpen: KVar<Boolean> = KVar(false)) {
    companion object {
        var modalCounter: Int = 0
    }

    fun close(){
        ec.browser.evaluate("""
            $('#${this.id}').modal('hide');
        """.trimIndent())

        isOpen.value = false
    }

    fun open(){
        isOpen.value = true
        ec.browser.evaluate("""
            $('#${this.id}').modal('show');
        """.trimIndent())
    }
}

fun ElementCreator<*>.modal(header: String, content: ElementCreator<DivElement>.(modal: ModalView) -> Unit) : ModalView {
    val mv = ModalView(this)

    val classes = fomantic.ui.modal
    render(mv.isOpen){isOpen ->
        if(isOpen) {
            div(classes.plus("id" to mv.id)).new {
                div(fomantic.ui.header).text(header)
                div(fomantic.ui.content).new {
                    content(mv)
                }
            }
        }
    }

    return mv
}

fun ElementCreator<*>.itemWithIcon(iconClass: FomanticUIClasses, header: String, description:String) = div(fomantic.ui.item).new {
    i(iconClass)
    div(fomantic.content).new {
        div(fomantic.header).text(header)
        span().text(description)
    }
}

class TabPane(val title:String, val block: ElementCreator<*>.()->Unit)
fun ElementCreator<*>.tab(vararg panes: TabPane){
    val activeItem = KVar(0)


    div(fomantic.ui.top.attached.tabular.menu).new {
        panes.forEachIndexed() {idx,pane->
            val element = a(fomantic.ui.item.active(idx == activeItem.value))
                .text(pane.title)
                .on.click {
                activeItem.value=idx
            }

            activeItem.addListener { oldIdx, newIdx ->
                if(oldIdx==idx && newIdx!=idx){
                    element.removeClasses("active")
                }
                if(oldIdx!=idx&&newIdx==idx){
                    element.addClasses("active")
                }
            }
        }
    }

    panes.forEachIndexed{idx, pane->
        div(fomantic.ui.bottom.attached.active(idx==activeItem.value).tab.segment).new {
            pane.block(this)

            activeItem.addListener { oldIdx, newIdx ->
                if(oldIdx==idx && newIdx!=idx){
                    this.parent.removeClasses("active")
                }
                if(oldIdx!=idx&&newIdx==idx){
                    this.parent.addClasses("active")
                }
            }
        }
    }
}