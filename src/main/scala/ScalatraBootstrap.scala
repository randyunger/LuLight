
import org.scalatra._
import javax.servlet.ServletContext

import org.runger.lulight.servlet.LutronServlet

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {

    context.mount(new LutronServlet, "/*")
  }
}
