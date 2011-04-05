package se.cygni.barbershop

import akka.util.TestKit
import akka.util.duration._
import akka.actor.Actor._
import org.specs.Specification

class BarberSpec extends Specification with TestKit with TestStubs {


  "A barber" should {

    val barber = actorOf(new Barber("Edward")).start

    doBefore {
      barber ! barbershop
      // A barber always send a Sleeping messages when started
      sign.expectMsg(100 millis, Sleeping)
    }

    doAfter {
      barber stop
    }

    "Respond with Cutting, CutDone and a NextCustomer after a RequestBarber(customer)" in {
      sign.sendMessage(barber, RequestBarber(customer1.ref))
      customer1.expectMsgAllOf(700 millis, Cutting, CutDone)
      chairs.expectMsg(100 millis, NextCustomer)
    }

    "Respond with Cutting, CutDone and a NextCustomer after a RequestBarber" in {
      customer1.sendMessage(barber, RequestBarber)
      customer1.expectMsgAllOf(700 millis, Cutting, CutDone)
      chairs.expectMsg(100 millis, NextCustomer)
    }

    "Respond with a Sleeping on a NoCustomersWaiting message" in {
      chairs.sendMessage(barber, NoCustomersWaiting)
      sign.expectMsg(100 millis, Sleeping)
      tracker.expectMsg(100 millis, TrackSleeping)
    }

  }
}