package org.runger.lulight

/**
  *
  * Created by Unger on 3/4/16.
  *
  **/

import akka.actor._
import akka.event.Logging
import akka.pattern.{ask, pipe}
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import org.eclipse.paho.client.mqttv3.{MqttClient, MqttConnectOptions}
import org.runger.lulight.MqttService.LevelChange

import scala.concurrent.Await
import scala.concurrent.duration._


object AkkaSystem {

  val sys = ActorSystem("LuLight")


}

object MqttService {
  import scala.concurrent.ExecutionContext.Implicits.global

  protected val sup = AkkaSystem.sys.actorOf(Props[MqttSupervisor], "supervisor")
  protected var lastAc: ActorRef = null
  protected def getActor() = {
    val f = sup.ask(Props[MqttClientActor])(1 second)
    val result = Await.result(f, 1 second).asInstanceOf[ActorRef]
    result
  }
//  protected val prodInstance = {
//    val ac = getActor()
//    new MqttService(ac)
//  }

  def apply() = {
    val ac = getActor()
    if(lastAc == null) {
      lastAc = ac
      AkkaSystem.sys.scheduler.scheduleOnce(10 seconds){
        lastAc ! new ArithmeticException
      }
    }
    new MqttService(ac)
  }

//  val host = "tcp://localhost:1883"
//  val clientId = "LuLight"  //Would have to make this unique for multiple servers.

  case class LevelChange(id: Int, loadState: LoadState)
}

class MqttService(ac: ActorRef) {
  def publish(id: Int, loadState: LoadState) = {
    val msg = LevelChange(id, loadState)
    ac ! msg
  }
}

//object MqttSupervisor {
//  val supervisor = AkkaSystem.sys.actorOf(Props[MqttSupervisor], "supervisor")
//}

class MqttSupervisor(implicit val bindingModule: BindingModule) extends Actor with Injectable {
  val logger = injectOptional [Logging] getOrElse { new LoggingImpl {} }

  override val supervisorStrategy =
//    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
    OneForOneStrategy() {
      case _: ArithmeticException      => {
        logger.info("Received Arthim Exc!")
        Resume
      }
//      case _: NullPointerException     => Restart
      case ex: java.net.ConnectException => {
        logger.warn("Connection exception!")
        ex.printStackTrace()
        Restart
      }
//      case ex: NoClassDefFoundError => {
//        log.warning("Could not initialize! Is Mqtt broker on1?")
//        Restart
//      }
//      case ex: java.lang.ExceptionInInitializerError => {
//        log.warning("Could not initialize! Is Mqtt broker on2?")
//        Restart
//      }
//      case ex: Exception                => {
//        log.warning("Generic exception! Escalating.1")
//        ex.printStackTrace()
//        Escalate
//      }
//
//      case x: Throwable                => {
//        log.warning("Generic exception! Escalating.2")
//        x.printStackTrace()
//        Escalate
//      }
    }

  def receive = {

    case e: Exception => {
      logger.info("Throwing exception for test purposes")
      throw e
    }

    case p: Props => {
      logger.info("Creating new Mqtt client actor")
      val newActor = context.actorOf(p)
      sender() ! newActor
    }
  }

}

class MqttClientActor(implicit val bindingModule: BindingModule) extends Actor with Injectable {

  val cl = new Mqtt(Mqtt.host, Mqtt.clientId) //Should we add something unique to the actor?

  def receive = {
    case LevelChange(id, loadState) => {
//      cl.publish(id, loadState)
    }
  }



}

