package org.lulight

import org.scalatra._
import scalate.ScalateSupport

class LutronServlet extends LuStack {

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  get("/on/:id") {
    val id  = params("id")
    val loads = LuConfig().search(id)
    loads.foreach (load =>{
      TelnetClient().execute(load.on())
    })
  }

  get("/off/:id") {
    val id  = params("id")
    val loads = LuConfig().search(id)
    loads.foreach (load =>{
      TelnetClient().execute(load.off())
    })
  }

}
