package gpio4s.pi4j

import akka.actor.{ActorContext, ActorRef}
import com.pi4j.io.gpio.{GpioFactory, GpioController, RaspiGpioProvider, RaspiPin}
import com.pi4j.io.gpio.impl.GpioPinImpl
import gpio4s.PinProducer

/**
 * Pi4j [[gpio4s.PinProducer]] implementation
 */
class Pi4jPinProducer(c: GpioController, p: RaspiGpioProvider) extends PinProducer {
    def get(num: Int)(implicit context: ActorContext): ActorRef = {
        context.actorOf(Pi4jGpio.props(new GpioPinImpl(c, p, RaspiPin.getPinByName("GPIO " + num))))
    }
}

object Pi4jPinProducer {
    def apply(): Pi4jPinProducer = new Pi4jPinProducer(GpioFactory.getInstance, new RaspiGpioProvider)
}
