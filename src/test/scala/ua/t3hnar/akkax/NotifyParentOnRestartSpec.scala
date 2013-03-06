package ua.t3hnar.akkax

import org.specs2.mutable.SpecificationWithJUnit
import akka.testkit.{TestActorRef, TestKit}
import akka.actor.{Props, Actor, ActorSystem}
import org.specs2.specification.Scope

/**
 * @author Yaroslav Klymko
 */
class NotifyParentOnRestartSpec extends SpecificationWithJUnit {

  import NotifyParentOnRestartSpec._

  "NotifyParentOnRestart" should {
    "notify parent actor on restart" in new ActorScope {
      restarted must beFalse
      actor ! Restart
      expectMsg(Restarted(actor.underlyingActor.child))
    }
  }


  abstract class ActorScope extends TestKit(ActorSystem()) with Scope {

    val actor = TestActorRef(new ParentActor)

    case object Restart

    val restarted = false

    class ParentActor extends Actor {
      lazy val child = context.actorOf(Props(new ChildActor))

      protected def receive = {
        case Restart => child ! new RuntimeException
        case restarted@Restarted(`child`) =>
          testActor ! restarted
      }
    }
  }
}

object NotifyParentOnRestartSpec {

  class ChildActor extends Actor with NotifyParentOnRestart {
    protected def receive = {
      case e: Exception => throw e
    }
  }
}