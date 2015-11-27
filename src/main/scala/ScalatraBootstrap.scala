import org.lulight._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {

    TelnetClient.init()

    context.mount(new LutronServlet, "/*")
  }
}
