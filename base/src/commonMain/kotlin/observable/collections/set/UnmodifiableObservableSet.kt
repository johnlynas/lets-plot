package jetbrains.datalore.base.observable.collections.set

import jetbrains.datalore.base.observable.collections.CollectionItemEvent
import jetbrains.datalore.base.observable.collections.CollectionListener
import jetbrains.datalore.base.observable.event.EventHandler
import jetbrains.datalore.base.registration.Registration

class UnmodifiableObservableSet<ElementT>(wrappedSet: ObservableSet<ElementT>) :
        UnmodifiableSet<ElementT>(wrappedSet), ObservableSet<ElementT> {

    override val wrappedSet: ObservableSet<ElementT>
        get() = super.wrappedSet as ObservableSet<ElementT>

    override fun addListener(l: CollectionListener<ElementT>): Registration {
        return wrappedSet.addListener(l)
    }

    override fun addHandler(handler: EventHandler<in CollectionItemEvent<ElementT>>): Registration {
        return wrappedSet.addHandler(handler)
    }

}