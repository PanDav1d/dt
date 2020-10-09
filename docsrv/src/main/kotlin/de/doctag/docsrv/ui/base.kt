package de.doctag.docsrv.ui

import de.doctag.docsrv.Resources
import de.doctag.docsrv.model.host
import de.doctag.docsrv.ui.modals.scanStatusModal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
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
            .footer {
                clear: both;
                position: relative;
                height: 20px;
                margin-top: -15px;
                width: 100%;
                display: table;
                text-align: center;
            }
            .footerContent {
                display: table-cell;
                vertical-align: middle;
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

    div(attributes = mapOf("class" to "footer")).new{
        div(attributes = mapOf("class" to "footerContent")).new{
            div(fomantic.ui.label).new {
                i(fomantic.ui.icon.server)
                span().text(this.browser.host())
            }
            div(fomantic.ui.label).new {
                i(fomantic.ui.icon.codeBranch)
                span().text("Build-Nr: ")
                span().text(Resources.load("version.txt"))
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
        element("script", mapOf("src" to "/ressources/html5-qrcode.min.js"))
    }

    div(fomantic.ui.fixed.menu).new{
        a(fomantic.header.item, href="/documents").new {
            img("/ressources/logo_small_inverse.svg", attributes = mapOf("width" to "32px", "height" to "32px"))
        }
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

            val scanModal = scanStatusModal{ sig ->

            }

            a(fomantic.item, href="#").apply { on.click { scanModal.open()} }.new {
                i(fomantic.ui.key.icon)
            }
            a(fomantic.item, href="/settings/users").new {
                i(fomantic.ui.cog.icon)
            }
            a(fomantic.item,href = "/logout").text("Abmelden")
        }

    }

    div(fomantic.ui.main.container).new {

        div(fomantic.column).new {
            div(fomantic.ui.vertical.segment).new {

                h1(fomantic.ui.header).text(title)
                div(fomantic.ui.content).new {
                    content(this,area)
                }

            }
        }
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