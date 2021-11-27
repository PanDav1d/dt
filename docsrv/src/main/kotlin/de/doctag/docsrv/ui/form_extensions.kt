package de.doctag.docsrv.ui

import com.github.salomonbrys.kotson.fromJson
import de.doctag.docsrv.model.DesignConfig
import kweb.*
import kweb.plugins.fomanticUI.FomanticUIClasses
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.property
import kweb.state.render
import kweb.util.random
import kweb.util.gson
import kotlin.math.abs

class FormControl{

    private val inputs: MutableList<FormInput> = mutableListOf()
    val errors: KVar<List<String>> = KVar(listOf())
    val formLevelValidations = mutableListOf<()->String?>()
    val submitActions = mutableListOf<()->Unit>()

    fun add(fi:FormInput){
        inputs.removeIf { fi.inputElement.id == it.inputElement.id }
        inputs.add(fi)
    }

    private fun validate(){
        logger.info("Running validations")
        errors.value = inputs.mapNotNull { it.checkInput() }.plus(formLevelValidations.mapNotNull { it.invoke() }).also {
            it.forEach { error->
                logger.warn(error)
            }
            logger.info("Done. Found ${it.size} errors")
        }
    }

    fun submit(){
        submitActions.map {
            it()
        }
    }

    val isValid: Boolean
        get() {
            validate()
            return errors.value.isEmpty()
        }

    fun withValidation(func: ()->String?){
        formLevelValidations.add(func)
    }

    fun withSubmitAction(func:()->Unit){
        submitActions.add(func)
    }
}

typealias ValidationFunc = (String?)->String?

interface FormInput {
    val actualValue: KVar<String>
    val isRequired: Boolean
    val label: String?
    val errorMessage: KVar<String?>
    val inputElement: Element

    fun runValidation(): String?
}

inline fun <T : FormInput> T.with(fc: FormControl) : T{


    fc.add(this)
    return this
}

fun FormInput.checkInput() : String?{
    errorMessage.value = runValidation()
    return errorMessage.value
}

class BasicFormInput(
    override val actualValue: KVar<String>,
    override val isRequired:Boolean,
    override val label:String?,
    private var validator: ValidationFunc?=null,
    override val errorMessage: KVar<String?> = KVar(null),
    private var inputMissingErrorMessage: String? = null) : FormInput{

    private lateinit var _inputElement: Element

    override val inputElement: Element
        get() = _inputElement


    fun setInputElement(e:Element){
        this._inputElement = e
    }


    fun withInputMissingErrorMessage(message:String) : FormInput{
        inputMissingErrorMessage = message
        return this
    }

    override fun runValidation(): String? {
        if(isRequired && actualValue.value.isBlank()){
            return inputMissingErrorMessage ?: "Das Feld '${label}' wird benötigt"
        }
        return validator?.invoke(actualValue.value)
    }

    fun validate(func: ValidationFunc): FormInput{
        validator = func
        return this
    }
}

class FileUpload(
        val fileName : String,
        val fileSize: String,
        val base64Content: String
)

class FileFormInput(
        override val actualValue: KVar<String>,
        override val isRequired:Boolean,
        override val label:String?,
        private var validator: ValidationFunc?=null,
        override val errorMessage: KVar<String?> = KVar(null),
        private var inputMissingErrorMessage: String? = null) : FormInput{

    private lateinit var _inputElement: Element

    override val inputElement: Element
        get() = _inputElement


    fun setInputElement(e:Element){
        this._inputElement = e
    }

    override fun runValidation(): String? {
        if(isRequired && actualValue.value.isBlank()){
            return inputMissingErrorMessage ?: "Das Feld '${label}' wird benötigt"
        }
        return validator?.invoke(actualValue.value)
    }

    fun onFileSelect(onFileSelectCallback: ()->Unit){
        inputElement.on.change { evt ->
            logger.info(evt.retrieved)
            onFileSelectCallback()
        }
    }

    fun retrieveFile(onFileRetrieveCallback:(FileUpload)->Unit){
        val callbackId = Math.abs(random.nextInt())

        val js = """
                let fd = document.getElementById("${inputElement.id}").files[0]
                let fr = new FileReader()
                fr.readAsDataURL(fd)
                fr.onload = function(){
                    callbackWs($callbackId,{base64Content: fr.result, fileSize: fd.size, fileName: fd.name});
                }
            """.trimIndent()

        inputElement.browser.executeWithCallback(js, callbackId) { result ->
            //logger.info("Result is ${result.toString()}")
            val fupload : FileUpload = gson.fromJson(result.toString())
            onFileRetrieveCallback( fupload )
        }
        inputElement.creator?.onCleanup(true) {
            inputElement.browser.removeCallback(callbackId)
        }
    }
}

class DropdownValueSelectEvent(val selectedValue: String?, val selectedText: String?)
class DropdownElement {
    internal val callbacks: MutableList<(key: String?)->Unit> = mutableListOf()
    fun onSelect(callback:(key: String?)->Unit)
    {
        callbacks.add(callback)
    }
}

fun ElementCreator<*>.dropdown(
    options: Map<String?, String>,
    currentValue: KVar<String?> = KVar(null),
    container: DivElement = div(fomantic.ui.selection.dropdown),
    itemRenderer:ElementCreator<DivElement>.(key:String?, displayText:String?)->Unit = { k,v-> span().text(v?:"") },
):DropdownElement {

    val result = DropdownElement()

    container.new {
        input(type=InputType.hidden, name="dropdown", initialValue = currentValue.value)
        i(fomantic.icon.dropdown)
        div(fomantic.text.default).text("Auswahl")
        div(fomantic.menu).new{
            options.forEach { (key, displayText) ->
                div(fomantic.item).apply { this.setAttributeRaw("data-value", key) }.new {
                    itemRenderer(key, displayText)
                }
            }
        }
    }


    val callbackId = abs(random.nextInt())
    browser.executeWithCallback("""
        $('#${container.id}').dropdown({
            action: 'activate',
            onChange: function(value, text) {
              // custom action
              console.log("changed")
              callbackWs($callbackId,{selectedValue: value, selectedText: text});
            }
        });
        """.trimIndent(), callbackId) {inputData->
        val selectedData : DropdownValueSelectEvent = gson.fromJson(inputData.toString())
        if(currentValue != null) {
            currentValue.value = selectedData.selectedValue ?: ""
        }
        result.callbacks.forEach{cb->cb.invoke(selectedData.selectedValue)}
    }

    return result
}

fun ElementCreator<*>.namedColorPicker(currentValue: KVar<String?>) {
    dropdown(mapOf(
        "red" to "Rot",
        "orange" to "Orange",
        "yellow" to "Gelb",
        "olive" to "Olivgrün",
        "green" to "Grün",
        "teal" to "Türkis",
        "blue" to "Blau",
        "violet" to "Violett",
        "pink" to "Magenta",
        "brown" to "Braun",
        "grey" to "Grau",
        "black" to "Schwarz",
        "" to "Keine"
    ), currentValue){ k, v->
        div(fomantic.ui.circle.label.withColor(k!!))
        span().text(v?:"")
    }
}


fun ElementCreator<*>.buttonWithAsyncLoader(label:String, classes: FomanticUIClasses = fomantic.ui.button, renderInline: Boolean=false,onClickAction: (whenDone: ()->Unit)->Unit) = useState(false, renderInline = renderInline) {isLoading, setLoading->
    logger.info("Value of isLoading: $isLoading. Classes ${classes.mapKeys { it }.toList()}")

    button(classes.loading(isLoading)).apply {
        text.value = label
        on.click {
            setLoading(true)
            onClickAction{
                logger.info("Set loading to false")
                setLoading(false)
            }
        }
    }
}

fun ElementCreator<*>.buttonWithLoader(label:String, classes: FomanticUIClasses= fomantic.ui.button, onClickAction: ()->Unit) = buttonWithAsyncLoader(label, classes){ whenDone->
    onClickAction()
    whenDone()
}

fun ElementCreator<*>.formSubmitButton(formCtrl: FormControl, label:String="Speichern", classes: FomanticUIClasses= fomantic.ui.button, submitAction: ()->Unit) = buttonWithLoader(label, classes) {
    if(formCtrl.isValid) {
        submitAction()
    }
}


fun ElementCreator<*>.radioInput(label:String?=null, options: Map<String,String>, required: Boolean=false, isInline:Boolean=false, bindTo: KVar<String>) : BasicFormInput {
    val formInput = BasicFormInput(bindTo, required, label)

    render(bindTo){
        div(fomantic.ui.required(required).fields.inline(isInline)).new {
            formInput.errorMessage.addListener { old, newError ->
                if(newError!=null){
                    this.parent.addClasses("error")
                }
                else{
                    this.parent.removeClasses("error")
                }
            }
            label?.let {
                label().text(label)
            }
            options.forEach{ (labelName, labelValue) ->
                div(fomantic.ui.field).new {
                    div(fomantic.ui.radio.checkbox.checked(labelValue == bindTo.value)).apply {
                        on.click {
                            bindTo.value = labelValue
                        }
                    }.new {
                        input(type = InputType.radio, name = labelValue, attributes = mapOf("class" to "hidden")).apply {
                            if(labelValue == bindTo.value){
                                checked(true)
                            }
                        }
                        label().text(labelName)
                    }
                }
            }
        }
    }

    return formInput
}

fun ElementCreator<*>.formInputWithRightLabel(label: String?=null, placeholder:String?=null, required:Boolean=false, bindTo: KVar<String>, rightLabelText: String, id: String?=null): BasicFormInput = absFormInput(label, required, bindTo){
    lateinit var input: InputElement
    div(fomantic.ui.right.labeled.input).new() {
        input = input(InputType.text, placeholder = placeholder, attributes = attr.plusElementIdValue(id)).apply { value=bindTo }
        div(fomantic.ui.label).text(rightLabelText)
    }
    input
}

fun ElementCreator<*>.formInput(label: String?=null, placeholder:String?=null, required:Boolean=false, bindTo: KVar<String>, inputType: InputType=InputType.text, id: String?=null): BasicFormInput = absFormInput(label, required, bindTo){
    lateinit var input: InputElement
    div(fomantic.ui.input).new() {
        input = input(inputType, placeholder = placeholder, attributes = attr.plusElementIdValue(id)).apply { value=bindTo }
    }
    input
}

private fun  <V> MutableMap<String, V>.plusElementIdValue(id: V?): Map<String, V> {
    return id?.let{this.plus("id" to id)} ?: this
}

fun ElementCreator<*>.checkBoxInput(label:String, bindTo: KVar<Boolean>) {
    val bindToStr = KVar(bindTo.toString())
    bindToStr.addListener { oldVal, newVal ->
        val currentVal = newVal.toBoolean()
        bindTo.value = currentVal
    }
    div(fomantic.field).new {
        render(bindTo){isChecked->
            div(fomantic.ui.checkbox.checked(bindTo.value))
                    .apply {
                        on.click {
                            bindTo.value = !bindTo.value
                        }
                    }
                    .new {
                        input(InputType.checkbox,attributes = mapOf("class" to "hidden")).apply {
                            if(isChecked) {
                                checked(true)
                            }
                        }
                        label().text(label)
                    }
        }
    }
}

fun ElementCreator<*>.absFormInput(label: String?=null, required:Boolean=false, bindTo: KVar<String>, inputElementFunc: ElementCreator<*>.()->InputElement) : BasicFormInput{
    val formInput = BasicFormInput(bindTo, required, label)


    div(fomantic.ui.required(required).field).new {

        formInput.errorMessage.addListener { old, newError ->
            if(newError!=null){
                this.parent.addClasses("error")
            }
            else{
                this.parent.removeClasses("error")
            }
        }

        label?.let {
            label().text(label)
        }

        val input = inputElementFunc()
        formInput.setInputElement(input)
    }
    return formInput
}

fun ElementCreator<*>.fileInput(label: String?=null, placeholder:String?=null, required:Boolean=false, bindTo: KVar<String>, accept:String? = null) : FileFormInput{
    val formInput = FileFormInput(bindTo, required, label)

    val class_ = if(required){
        fomantic.ui.required.field
    } else {
        fomantic.ui.field
    }

    div(class_).new {

        formInput.errorMessage.addListener { old, newError ->
            if(newError!=null){
                this.parent.addClasses("error")
            }
            else{
                this.parent.removeClasses("error")
            }
        }

        label?.let {
            label().text(label)
        }
        div(fomantic.ui.input).new() {
            val attrs = if( accept!= null){
                mapOf("accept" to accept)
            } else {
                mapOf()
            }
            val input = input(InputType.file, placeholder = placeholder, attributes = attrs)
            formInput.setInputElement(input)
        }
    }

    return formInput
}


fun ElementCreator<*>.formControl(block: ElementCreator<*>.(form:FormControl)->Unit) : FormControl {
    val fc = FormControl()

    form(fomantic.ui.form).new(){
        fc.errors.addListener { old, new ->
            logger.info("Form Error state did change.")

            if(old.isNotEmpty() && new.isEmpty()){
                this.parent.removeClasses("error")
                logger.info("Removing Error class")
            }
            if(old.isEmpty() && new.isNotEmpty()){
                this.parent.addClasses("error")
                logger.info("Adding Error class")
            }
        }

        block(fc)
    }

    return fc
}


fun ElementCreator<*>.displayErrorMessages(form:FormControl)  {
    render(form.errors){errors ->
        if(errors.isNotEmpty()) {
            div(fomantic.ui.message).new {
                div(fomantic.ui.header).text("Bitte überprüfen Sie Ihre Eingaben.")
                ul(fomantic.ui.list).new {
                    errors.forEach { error ->
                        li().text(error)
                    }
                }
            }
            div(fomantic.ui.divider.hidden)
        }
    }
}