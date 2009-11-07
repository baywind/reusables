/* MultiECLockManager.java created by j-rochkind@northwestern.edu on Mon 21-Jul-2003 */

/*
   Meant to handle locking of 'local' EOEditingContexts you use in a session.
   Each WOSession should have one of these MultiECLockManager objects. The session
   should call lock() on it in awake() and unlock() in sleep().

   When 'local' ECs are created, they should be registered with the Session's
   MultiECLockManagers. [For Collaboratory Project developers, the LocalECManager
   class takes care of this for you.]

   The only public methods are lock(), unlock(), registerEditingContext() and
   unregisterEditingContext().

   A particular thread can call lock() multiple times, as long it calls unlock()
   the same number of times to relinquish all locks---that is, lock() does
   function recursively.

   If a particular thread has outstanding lock() calls not undone with unlock(),
   only that thread may call unlock() or registerEditingContext(). If any other
   thread calls these methods in this state, an exception will be raised. But
   in the intended design, this shouldn't be possible anyway. Any thread may call
   unregisterEditingContext at any time---ECs are queued up for unregistering at
   an appropriate time and in an appropriate thread, if neccesary.
*/

package net.rujel.reusables;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

import java.lang.ref.WeakReference;


public class MultiECLockManager {
    protected NSMutableSet weakReferences = new NSMutableSet();
    protected NSMutableDictionary strongReferences = new NSMutableDictionary();
    protected NSMutableArray unregisterQueue = new NSMutableArray();
    protected int lockCount = 0;
    protected Thread lockerThread;


    //Locks all registered ECs. Creates strong references to registered ECs,
    //to ensure they don't get garbage collected with outstanding locks.
    //Records which thread has requested the lock.
    public synchronized void lock() {
        //If used as intended, with lock() and unlock() happening in session awake()
        //and sleep(), or equivelent----then I don't think there's any way
        //that thread2 can be attempting to lock() while thread1 has lock()ed
        //and not yet unlock()ed. However, if this DOES happen---I'm not confident
        //this code is deadlock-safe. So just to be sure, we throw an exception
        //if it does happen, even though we don't believe it ever should. If the exception
        //is thrown, it really indicates that this code needs to be redesigned.
        sameThreadAssert("Attempt to lock from a second thread while locked from another.");

        //We make strong references to all ECs that we are going to lock,
        //to keep them from getting Garbage Collected while they have
        //outstanding locks. In the case of Nested ECs especially,
        //it's disastrous to have them GC'd with outstanding locks.
        makeStrongReferences();

        //Now we go through all the registered ECs and lock them one by one.
        NSArray allECs = strongReferences.allValues();
        for ( int i = allECs.count() - 1; i >= 0; i--) {
            EOEditingContext ec = (EOEditingContext) allECs.objectAtIndex( i );
            ec.lock();
        }
        //We record which thread is doing the lock, because some operations
        //may be dependent on this, we may want to disallow some operations
        //from a different thread, while outstanding locks exist.
        lockerThread = Thread.currentThread();
        lockCount++;
    }

    //Throws an exception if no locks are outstanding, or if a thread other
    //than the one which locked tries to unlock.
    //Unlocks all registered ECs.
    //If we have now called unlock() as many times as we've called lock(),
    //remove all strong references, forget the locking thread, and unregister
    //any ECs previously put in the queue to unregister.
    public synchronized void unlock() {
        if ( lockCount <= 0) {
            throw new IllegalStateException("MultiECLockManager: Attempt to unlock without a previous lock!");
        }
        sameThreadAssert("Can't unlock from different thread than locked!");


        //We know we have strong references already set, because we did that
        //upon locking.
        NSArray allECs = strongReferences.allValues();
        for ( int i = allECs.count() - 1; i >= 0; i--) {
            EOEditingContext ec = (EOEditingContext) allECs.objectAtIndex( i );
            ec.unlock();
        }

        lockCount--;

        if ( lockCount == 0 ) {
            //unregister anyone we have waiting in the queue.
            emptyUnregisterQueue();
            //We don't need the strong references anymore, we have no locks.
            strongReferences.removeAllObjects();
            //And we have no locker thread anymore, we're done with locks
            //for the moment.
            lockerThread = null;
        }
    }


    public void fullyUnlock() {
    	while (lockCount > 0) {
			unlock();
		}
    }

    //Registers EC. If other registered ECs are currently locked by us,
    //lock the newly registered EC the same number of times to match outstanding
    //locks on other registered ECs. If there are locks outstanding,
    //and a thread other than the one which locked is attempting
    //to register an EC, throw an exception.  If the EC argument is ALREADY
    //registered, do nothing.
    public synchronized void registerEditingContext(EOEditingContext ec) {
        //If we are currently locked, and ANOTHER thread is trying to
        //register an EC, that's very bad. Let's throw an exception.
        sameThreadAssert("When ECs are locked, can't register an EC from another thread!");

        //Make sure we catch a double registration--we don't want to have
        //it registered twice!
        WeakReference alreadyRegistered = findReference(ec);
        if ( alreadyRegistered != null ) {
            //throw or ignore? Let's just ignore. We don't need to register
            //it again, it's already registered.
            return;
        }

        WeakReference ref = new WeakReference( ec );
        weakReferences.addObject( ref );

        //If we already have some locks, and we're registering a new EC,
        //let's lock it the proper number of times to bring it up to speed.
        //Remember, we made sure we are in the lockerThread.
        if ( lockCount > 0 ) {
            strongReferences.setObjectForKey( ec, ref );
            for ( int i = 0; i < lockCount; i++) {
                ec.lock();
            }
        }
    }

    //Unregisters EC from receiver. If the EC is not currently registered, do nothing.
    //If there are outstanding locks, do not unregister the EC immediately, instead
    //put it in a queue to unregister. The queue will be handled when there are
    //no longer outstanding locks. This means it should be safe to unregister an
    //EC in some other objects finalize() method, even though that will be in
    //an unpredictable thread.
    public synchronized void unregisterEditingContext(EOEditingContext argEC) {
        //Find it's weak reference
        WeakReference correspondingReference = findReference(argEC);
        if ( correspondingReference == null ) {
            //Hmm, it's not registered. Ignore, or throw?
            //Let's ignore.
            return;
        }
        //we need to unlock it same number of times we locked it.
        //This actually only matters with Nested ECs, but it really
        //does matter with them. We'll handle this by just adding
        //it to a queue to deregister later after we've completely
        //unlocked---this way we don't need to worry about what
        //thread is calling unregister, if it's a different thread
        //than the one that locked.
        if ( lockCount > 0 ) {
            unregisterQueue.addObject( correspondingReference );
        }
        else {
            //No outstanding locks, we can deregister immediately.
            weakReferences.removeObject( correspondingReference );
        }
    }


    //Not meant to be called externally. Makes strong references to all registered ECs,
    //by putting them in a dictionary keyed by their weak reference. This is the method
    //that will remove stale EC WeakReferences, if they're referent has been garbage
    //collected.
    protected synchronized void makeStrongReferences() {
        NSArray weakReferencesArr = weakReferences.allObjects();
        for ( int i = weakReferencesArr.count() - 1; i >= 0; i--) {
            WeakReference ref = (WeakReference) weakReferencesArr.objectAtIndex( i );
            EOEditingContext ec = (EOEditingContext) ref.get();
            if ( ec == null ) {
                //Okay, it's been garbage collected, let's remove
                //the reference from the list, we don't need it anymore.
                weakReferences.removeObject( ref );
            }
            else {
                //Store the strong reference in a dictionary, to keep
                //it from being collectd. Reference it by weak reference.
                strongReferences.setObjectForKey( ec, ref );
            }

        }
    }
    //Not meant to be called externally. Since we often are only holding
    //onto a WeakReference to our registered ECs, to allow ECs to be GC'd
    //even though they are registerd, this is a convenience method to find
    //the appropriate WeakReference given an EC that's registered.  If the argument
    //is NOT a registered EC, will return null.
    protected synchronized WeakReference findReference(EOEditingContext argEC) {
        WeakReference correspondingReference = null;
        NSArray weakReferencesArr = weakReferences.allObjects();
        for ( int i = weakReferencesArr.count() - 1; i >= 0; i--) {
            WeakReference ref =
            (WeakReference) weakReferencesArr.objectAtIndex( i );
            EOEditingContext checkEC = (EOEditingContext) ref.get();
            if ( checkEC != null && checkEC == argEC ) {
                correspondingReference = ref;
                break;
            }
        }
        return correspondingReference;
    }

    //Not meant to be called externally.
    //Any ECs that had unregistration requested with outstanding locks, just
    //get added to the unregisterQueue. When there are no longer outstanding
    //locks, this method is called to unregister all those ECs.
    protected synchronized void emptyUnregisterQueue() {
        for ( int i = unregisterQueue.count() - 1; i >= 0; i--) {
            WeakReference ref = (WeakReference) unregisterQueue.objectAtIndex( i );
            weakReferences.removeObject( ref );
            unregisterQueue.removeObjectAtIndex( i );
        }
    }

    //Checks that if there is a positive lockCount, the current thread attempting
    //some operation in the MultiECLockManager is the same thread that made
    //the lock().  In general, for intended uses of the MultiECLockManager,
    //this should always be true. But code that requires it to be true
    //calls this assert message just in case. If an exception is thrown,
    //it either indicates a problem with the MultiECLockManager code (possibly
    //requiring a redesign so the code does NOT require this assert),
    //or a problem with developer code using this object.
    private void sameThreadAssert(String messageAddition) throws IllegalStateException {
        if ( lockCount > 0 ) {
            Thread currentThread = Thread.currentThread();
            if ( currentThread != lockerThread ) {
                throw new IllegalStateException("MultiECLockManager: " + messageAddition + "; current thread: " + currentThread.getName() + "; original locking thread: " + lockerThread.getName());
            }
        }
    }

    //Interface for a session to advertise that it has a MultiECLockManager
    //used for locking all 'local' ECs within that session. This interface
    //is only meant to be implemented by a WOSession, and that WOSession
    //subclass is responsible for locking/unlcoking the MultiECLockManager
    //in appropriate places. (awake() and sleep() reccomended ). 
    public static interface Session {
        public MultiECLockManager ecLockManager();
    }
}
