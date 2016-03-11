package org.jetbrains.haskell.debugger.utils

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Used to synchronize threads in situation when one side (waiting side) want ot know when other side (receiver side)
 * finished it's work. Waiting side should create the instance and after making lock by calling lock()
 * on it and sending the instance to receiver side start waiting signal by calling await(). await() call should be
 * enclosed by while(!syncObject.signaled()) to avoid effect of spurious wakeups. Receiver side after getting
 * SyncObject instance should lock it and after finishing it's work call signal() method to notify waiting side
 * and unlock() method to let waiting side continue it's execution.
 *
 * @author Habibullin Marat
 */
class SyncObject() {
    private val lock: Lock = ReentrantLock()
    private val condition: Condition = lock.newCondition()
    private var conditionSignaled: Boolean = false

    fun lock(): Unit = lock.lock()
    fun unlock(): Unit = lock.unlock()
    fun signaled(): Boolean = conditionSignaled
    fun signal(): Unit {
        conditionSignaled = true
        condition.signal()
    }
    fun await(): Unit = condition.await()
}