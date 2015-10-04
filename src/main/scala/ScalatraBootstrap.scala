import org.lulight._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {

  val tc = TelnetClient()
  val lu = LuConfig()

  override def init(context: ServletContext) {
    context.mount(new LutronServlet, "/*")
  }
}
