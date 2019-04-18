package jetbrains.datalore.base.observable.collections.set

import jetbrains.datalore.base.observable.collections.CollectionAdapter
import jetbrains.datalore.base.observable.collections.CollectionItemEvent
import jetbrains.datalore.base.observable.collections.CollectionListener
import jetbrains.datalore.base.observable.event.EventHandler
import jetbrains.datalore.base.observable.event.ListenerCaller
import jetbrains.datalore.base.observable.event.Listeners
import jetbrains.datalore.base.registration.Registration

abstract class AbstractObservableSet<ItemT> : AbstractMutableSet<ItemT>(), ObservableSet<ItemT> {
    private var myListeners: Listeners<CollectionListener<ItemT>>? = null
    abstract val actualIterator: MutableIterator<ItemT>

    override fun addListener(l: CollectionListener<ItemT>): Registration {
        if (myListeners == null) {
            myListeners = Listeners()
        }
        return myListeners!!.add(l)
    }

    override fun add(element: ItemT): Boolean {
        if (contains(element)) return false
        doBeforeAdd(element)
        var success = false
        try {
            onItemAdd(element)
            success = doAdd(element)
        } finally {
            doAfterAdd(element, success)
        }
        return success
    }

    private fun doBeforeAdd(item: ItemT) {
        checkAdd(item)
        beforeItemAdded(item)
    }

    private fun doAfterAdd(item: ItemT, success: Boolean) {
        try {
            if (success && myListeners != null) {
                myListeners!!.fire(object : ListenerCaller<CollectionListener<ItemT>> {
                    override fun call(l: CollectionListener<ItemT>) {
                        l.onItemAdded(CollectionItemEvent(null, item, -1, CollectionItemEvent.EventType.ADD))
                    }
                })
            }
        } finally {
            afterItemAdded(item, success)
        }
    }

    override fun remove(element: ItemT): Boolean {
        if (!contains(element)) return false
        doBeforeRemove(element)
        var success = false
        try {
            onItemRemove(element)
            success = doRemove(element)
        } finally {
            doAfterRemove(element, success)
        }
        return success
    }

    override fun iterator(): MutableIterator<ItemT> {
        if (size == 0) {
            return mutableSetOf<ItemT>().iterator()
        }
        val iterator = actualIterator
        return object : MutableIterator<ItemT> {
            private var myCanRemove = false
            private var myLastReturned: ItemT? = null

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): ItemT {
                myLastReturned = iterator.next()
                myCanRemove = true
                return myLastReturned!!
            }

            override fun remove() {
                if (!myCanRemove) {
                    throw IllegalStateException()
                }
                myCanRemove = false
                doBeforeRemove(myLastReturned!!)
                var success = false
                try {
                    iterator.remove()
                    success = true
                } finally {
                    doAfterRemove(myLastReturned!!, success)
                }
            }
        }
    }

    private fun doBeforeRemove(item: ItemT) {
        checkRemove(item)
        beforeItemRemoved(item)
    }

    private fun doAfterRemove(item: ItemT, success: Boolean) {
        try {
            if (success && myListeners != null) {
                myListeners!!.fire(object : ListenerCaller<CollectionListener<ItemT>> {
                    override fun call(l: CollectionListener<ItemT>) {
                        l.onItemRemoved(CollectionItemEvent(item, null, -1, CollectionItemEvent.EventType.REMOVE))
                    }
                })
            }
        } finally {
            afterItemRemoved(item, success)
        }
    }

    protected abstract fun doAdd(item: ItemT): Boolean
    protected abstract fun doRemove(item: ItemT): Boolean

    protected fun checkAdd(item: ItemT?) {}

    protected fun checkRemove(item: ItemT?) {}

    protected fun beforeItemAdded(item: ItemT?) {}

    protected fun onItemAdd(item: ItemT?) {}

    protected fun afterItemAdded(item: ItemT?, success: Boolean) {}

    protected fun beforeItemRemoved(item: ItemT?) {}

    protected fun onItemRemove(item: ItemT) {}

    protected fun afterItemRemoved(item: ItemT?, success: Boolean) {}


    override fun addHandler(handler: EventHandler<in CollectionItemEvent<ItemT>>): Registration {
        return addListener(object : CollectionAdapter<ItemT>() {
            override fun onItemAdded(event: CollectionItemEvent<ItemT>) {
                handler.onEvent(event)
            }

            override fun onItemRemoved(event: CollectionItemEvent<ItemT>) {
                handler.onEvent(event)
            }
        })
    }
}