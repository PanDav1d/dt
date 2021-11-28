import de.doctag.docsrv.model.AttachedTag
import de.doctag.docsrv.ui.*
import de.doctag.docsrv.ui.forms.system.addTagDropdown
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kweb.*
import kweb.plugins.fomanticUI.fomantic
import kweb.state.KVar
import kweb.state.render
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class SearchFilter(
    val searchString: String,
    val fromDate: ZonedDateTime? = null,
    val tillDate : ZonedDateTime? = null,
    val isExpanded: Boolean = false,
    val tags: List<AttachedTag>? = null
)

fun ElementCreator<*>.documentSearchFilterComponent(currentValue : SearchFilter = SearchFilter(""), onFilterChange: (sf:SearchFilter)->Unit) = useState(currentValue) { currentValue, setCurrentValue->

    fun setValue(sf:SearchFilter){
        setCurrentValue(sf)
        onFilterChange(sf)
    }

    val searchTerm = KVar(currentValue?.searchString ?:"")

    searchTerm.addListener { old, newVal ->
        setValue(currentValue.copy(searchString= newVal))
    }


    div(fomantic.ui.input.action.fluid.mini).new {
        input(InputType.text, placeholder = "suche").apply {
            value=searchTerm
        }.focus()
        button(fomantic.ui.icon.button.mini.active(currentValue.isExpanded)).apply{
            on.click {
                setCurrentValue(currentValue.copy(isExpanded = !currentValue.isExpanded))
            }
        }.new {
            i(fomantic.icon.filter)
        }
    }

    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val dateFrom = KVar(currentValue?.fromDate?.format(df)?:"")
    val dateTo = KVar(currentValue?.tillDate?.format(df)?:"")
    dateFrom.addListener { old, new ->
        logger.info("Selected from Date: $new")
        if(new.isNotEmpty() && dateTo.value.isNotEmpty()){
            val from = LocalDate.parse(dateFrom.value, df).atStartOfDay(ZoneId.of("Europe/Berlin"))
            val till = LocalDate.parse(dateTo.value, df).atStartOfDay(ZoneId.of("Europe/Berlin")).plusDays(1)

            logger.info("Trigger onFilterChange")
            setValue(currentValue.copy(fromDate = from, tillDate = till))
        }
    }

    dateTo.addListener { old, new ->
        logger.info("Selected to Date: $new")
        if(dateFrom.value.isNotEmpty() && new.isNotEmpty()){
            val from = LocalDate.parse(dateFrom.value, df).atStartOfDay(ZoneId.of("Europe/Berlin"))
            val till = LocalDate.parse(dateTo.value, df).atStartOfDay(ZoneId.of("Europe/Berlin")).plusDays(1)

            logger.info("Trigger onFilterChange")
            setValue(currentValue.copy(fromDate = from, tillDate = till))
        }
    }

    if(currentValue.isExpanded) {


        div(fomantic.ui.grid.withStyle("padding-top:8px;")).new {
            div(fomantic.row.withStyle("padding-bottom:4px;")).new {
                div(fomantic.eight.wide.column.withStyle("padding-right:4px;")).new {
                    div(fomantic.ui.input.mini.withStyle("width: 100%;")).new {
                        input(type = InputType.date, placeholder = "Von",attributes = mapOf("style" to "width: 40rw;")).apply {
                            value=dateFrom
                        }
                    }
                }
                div(fomantic.eight.wide.column.withStyle("padding-left:4px;")).new {
                    div(fomantic.ui.input.mini.withStyle("width: 100%;")).new {
                        input(type = InputType.date, placeholder = "Bis",attributes = mapOf("style" to "width: 40rw;")).apply {
                            value=dateTo
                        }
                    }
                }
            }

            div(fomantic.row.withStyle("padding-top:4px;")).new {
                div(fomantic.sixteen.wide.column).new {

                    currentValue.tags?.forEach {
                        tag(it, true ){
                            val newTagList = currentValue.tags.filter { t->t._id != it._id }
                            currentValue.copy(tags = newTagList).let {
                                setValue(it)
                            }
                        }
                    }
                    addTagDropdown(currentValue.tags?: listOf()){
                        val newTags = (currentValue.tags?: listOf()).plus(it.asAttachedTag())
                        currentValue.copy(tags = newTags)?.let {
                            setValue(it)
                        }
                    }
                }
            }
        }
    }

}