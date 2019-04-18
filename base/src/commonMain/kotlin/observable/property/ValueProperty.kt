package jetbrains.datalore.base.observable.property

import jetbrains.datalore.base.observable.event.EventHandler
import jetbrains.datalore.base.observable.event.ListenerCaller
import jetbrains.datalore.base.observable.event.Listeners
import jetbrains.datalore.base.registration.Registration
import kotlin.jvm.JvmOverloads

/**
 * A simple implementation of Read/Write property which stores the value in a field
 */
open class ValueProperty<ValueT>
@JvmOverloads
constructor(private var myValue: ValueT) :
        BaseReadableProperty<ValueT>(),
        Property<ValueT> {

    private var myHandlers: Listeners<EventHandler<in PropertyChangeEvent<out ValueT>>>? = null

    override val propExpr: String
        get() = "valueProperty()"

    override fun get(): ValueT {
        return myValue
    }

    override fun set(value: ValueT) {
        if (value == myValue) return
        val oldValue = myValue
        myValue = value

        fireEvents(oldValue, myValue)
    }

    private fun fireEvents(oldValue: ValueT?, newValue: ValueT?) {
        if (myHandlers != null) {
            val event = PropertyChangeEvent(oldValue, newValue)
            myHandlers!!.fire(object : ListenerCaller<EventHandler<in PropertyChangeEvent<out ValueT>>> {
                override fun call(l: EventHandler<in PropertyChangeEvent<out ValueT>>) {
                    l.onEvent(event)
                }
            })
        }
    }

    override fun addHandler(handler: EventHandler<in PropertyChangeEvent<out ValueT>>): Registration {
        if (myHandlers == null) {
            myHandlers = object : Listeners<EventHandler<in PropertyChangeEvent<out ValueT>>>() {
                override fun afterLastRemoved() {
                    myHandlers = null
                }
            }
        }

        return myHandlers!!.add(handler)
    }
}