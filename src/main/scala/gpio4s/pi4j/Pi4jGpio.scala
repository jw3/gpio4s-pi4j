package gpio4s.pi4j

import akka.actor.{Actor, Props}
import com.pi4j.io.gpio.PinMode
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.impl.GpioPinImpl
import gpio4s._
import gpio4s.gpiocfg.CfgIO.PinCfg

/**
 * Actor interface to Pi4j GPIO
 */
class Pi4jGpio(gpio: GpioPinImpl) extends Actor {
    def digitalIn: Receive = {
        case DigitalRead(_) => sender() ! gpio.isHigh
        case Reset(_) => reset()
    }

    def digitalOut: Receive = {
        case DigitalWrite(_, s) => gpio.setState(s)
        case Reset(_) => reset()
    }

    def receive: Receive = {
        case Setup(p) => setup(p)
    }

    def setup(pin: PinCfg) = {
        pin.mode match {
            case digital if pin.dir.isInput => {
                gpio.export(PinMode.DIGITAL_INPUT)
                gpio.addListener(stateChangeListener)
                context.become(digitalIn)
            }
            case digital if pin.dir.isOutput => {
                gpio.export(PinMode.DIGITAL_OUTPUT)
                context.become(digitalOut)
            }
        }
    }

    lazy val stateChangeListener = { e: GpioPinDigitalStateChangeEvent =>
        context.parent ! DigitalEvent(gpio.getPin.getAddress, e.getState.isHigh)
    }

    def reset(): Unit = {
        gpio.removeListener(stateChangeListener)
        context.become(receive)
    }
}

object Pi4jGpio {
    def props(gpio: GpioPinImpl): Props = Props(new Pi4jGpio(gpio))
}
