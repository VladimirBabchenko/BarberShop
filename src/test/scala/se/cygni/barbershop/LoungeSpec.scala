package se.cygni.barbershop

import akka.util.duration._
import akka.actor.Actor._
import org.specs.Specification
import akka.util.TestKit
import collection.script.Update

class LoungeSpec extends Specification with TestKit with TestStubs {

  "The chairs" should {

    val chairs = actorOf(new Lounge(2)).start

    doBefore {
      chairs ! barbershop
    }
    doAfter {
      chairs stop
    }

    val timeout = 200 millis


    "Forward a RequestBaraber message all chairs are empty" in {
      line.sendMessage(chairs, RequestBarber(customer1.ref))
      sign.expectMsg(timeout, RequestBarber(customer1.ref))
    }

    "Send a TakeChair messages to the original customer on a Wait message" in {
      sign.sendMessage(chairs, Wait(customer1.ref))
      customer1.expectMsg(timeout, TakeChair(0))
    }

    "Send a TakeChair message on a RequestBarber message  if there are free chairs" in {
      sign.sendMessage(chairs, Wait(customer1.ref))
      customer1.expectMsg(timeout, TakeChair(0))
      line.sendMessage(chairs, RequestBarber(customer2.ref))
      customer2.expectMsg(TakeChair(1))
     // tracker.expectMsg(Update(chairs))
    }

    "Send a Wait message to the line on  a Wait messages if all chairs are taken" in {
      sign.sendMessage(chairs, Wait(customer1.ref))
      customer1.expectMsg(timeout, TakeChair(0))
      sign.sendMessage(chairs, Wait(customer2.ref))
      customer2.expectMsg(timeout, TakeChair(1))
      line.sendMessage(chairs, RequestBarber(customer3.ref))
      line.expectMsg(timeout, Wait(customer3.ref))
    }

    "Send a NextCustomer to Line when receiving NextCustomer message if all seats are empty" in {
      sign.sendMessage(chairs, NextCustomer)
      barber1.expectNoMsg(timeout)
      sign.expectNoMsg(timeout)
      line.expectMsg(timeout, NextCustomer)
    }

    "When a barber sends a NextCustomer, send a RequestBaraber to the sign with the first waiting customer, TrackLeftChair to the Tracker and a NextCustomer to the waiting line" in {
      sign.sendMessage(chairs, Wait(customer1.ref)) // Queue one customer
      customer1.expectMsg(timeout, TakeChair(0))
      barber1.sendMessage(chairs, NextCustomer)
      sign.expectMsg(timeout, RequestBarber(customer1.ref))
      tracker.expectMsg(timeout, TrackLeftChair(0))
      line.expectMsg(timeout, NextCustomer)
    }
  }
}
