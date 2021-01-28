import de.doctag.docsrv.ui.active
import de.doctag.docsrv.ui.calendar
import de.doctag.docsrv.ui.filter
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

data class SearchFilter(val searchString: String, val fromDate: ZonedDateTime? = null, val tillDate : ZonedDateTime? = null, val isExpanded: Boolean = false)

fun ElementCreator<*>.documentSearchFilterComponent(currentValue : SearchFilter? = null, onFilterChange: (sf:SearchFilter)->Unit) {

    val showExpanded = KVar(currentValue?.isExpanded ?: false)
    val searchTerm = KVar(currentValue?.searchString ?:"")
    searchTerm.addListener { old, newVal ->
        onFilterChange(SearchFilter(newVal, isExpanded = showExpanded.value))
    }


    render(showExpanded){ shouldShowExpanded ->

        div(fomantic.ui.input.action.fluid.mini).new() {
            input(InputType.text, placeholder = "suche").apply {
                value=searchTerm
            }.focus()
            button(fomantic.ui.icon.button.mini.active(shouldShowExpanded)).apply{
                on.click {
                    showExpanded.value = !showExpanded.value
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
                onFilterChange(SearchFilter(searchTerm.value, from, till, true))
            }
        }

        dateTo.addListener { old, new ->
            logger.info("Selected to Date: $new")
            if(dateFrom.value.isNotEmpty() && new.isNotEmpty()){
                val from = LocalDate.parse(dateFrom.value, df).atStartOfDay(ZoneId.of("Europe/Berlin"))
                val till = LocalDate.parse(dateTo.value, df).atStartOfDay(ZoneId.of("Europe/Berlin")).plusDays(1)

                logger.info("Trigger onFilterChange")
                onFilterChange(SearchFilter(searchTerm.value, from, till, true))
            }
        }

        if(shouldShowExpanded) {

            div(fomantic.ui.divider.hidden.mini)

            div(fomantic.ui.grid.compact).new {
                div(fomantic.eight.wide.column).new {
                    div(fomantic.ui.input.mini).new {
                        input(type = InputType.date, placeholder = "Von",attributes = mapOf("style" to "width: 40rw;")).apply {
                            value=dateFrom
                        }
                    }
                }
                div(fomantic.eight.wide.column).new {
                    div(fomantic.ui.input.mini).new {
                        input(type = InputType.date, placeholder = "Bis",attributes = mapOf("style" to "width: 40rw;")).apply {
                            value=dateTo
                        }
                    }
                }
            }
        }
    }
}