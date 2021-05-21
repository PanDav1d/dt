import de.doctag.docsrv.kwebFeature
import de.doctag.docsrv_api.invoker.ApiException
import de.doctag.lib.logger
import io.ktor.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import kotlin.concurrent.thread

val TESTING_PORT = 61123

class WithTestingHttpServer : BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    override fun beforeAll(context: ExtensionContext) {
        if (!started) {
            started = true
            // Your "before all tests" startup logic goes here

            logger.info("Before test triggered")
            server = embeddedServer(Jetty, host = "0.0.0.0", port = TESTING_PORT, module = Application::kwebFeature)

            thread(start = true, isDaemon = true) {
                server.start()
            }

            val api = setupApi()
            while(true){
                try {
                    if (api.discoverInstance() != null) {
                        break;
                    }
                }catch(ex:ApiException){
                    Thread.sleep(100)
                }
            }

            // The following line registers a callback hook when the root test context is shut down
            context.getRoot().getStore(GLOBAL).put("TestingHttpServer", this)
        }
    }

    override fun close() {
        // Your "after all tests" logic goes here
        server.stop(1000,1500)
    }

    companion object {
        private var started = false
        private lateinit var server : JettyApplicationEngine
    }
}