package com.nostra13.universalimageloader.core.assist.deque;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LinkedBlockingDeque<E> extends AbstractQueue<E> implements BlockingDeque<E>, Serializable, Queue {
    private static final long serialVersionUID = -387911632671998426L;
    private final int capacity;
    private transient int count;
    transient Node<E> first;
    transient Node<E> last;
    final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    private abstract class AbstractItr implements Iterator<E> {
        private Node<E> lastRet;
        Node<E> next;
        E nextItem;
        final /* synthetic */ LinkedBlockingDeque this$0;

        abstract Node<E> firstNode();

        abstract Node<E> nextNode(Node<E> node);

        AbstractItr(LinkedBlockingDeque linkedBlockingDeque) {
            Object obj = null;
            this.this$0 = linkedBlockingDeque;
            ReentrantLock lock = linkedBlockingDeque.lock;
            lock.lock();
            try {
                this.next = firstNode();
                if (this.next != null) {
                    obj = this.next.item;
                }
                this.nextItem = obj;
            } finally {
                lock.unlock();
            }
        }

        private Node<E> succ(Node<E> n) {
            while (true) {
                Node<E> s = nextNode(n);
                if (s == null) {
                    return null;
                }
                if (s.item != null) {
                    return s;
                }
                if (s == n) {
                    return firstNode();
                }
                n = s;
            }
        }

        void advance() {
            Object obj = null;
            ReentrantLock lock = this.this$0.lock;
            lock.lock();
            try {
                this.next = succ(this.next);
                if (this.next != null) {
                    obj = this.next.item;
                }
                this.nextItem = obj;
            } finally {
                lock.unlock();
            }
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public E next() {
            if (this.next != null) {
                this.lastRet = this.next;
                E x = this.nextItem;
                advance();
                return x;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            Node<E> n = this.lastRet;
            if (n != null) {
                this.lastRet = null;
                ReentrantLock lock = this.this$0.lock;
                lock.lock();
                try {
                    if (n.item != null) {
                        this.this$0.unlink(n);
                    }
                    lock.unlock();
                } catch (Throwable th) {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private class Itr extends AbstractItr {
        private Itr() {
            super(LinkedBlockingDeque.this);
        }

        Node<E> firstNode() {
            return LinkedBlockingDeque.this.first;
        }

        Node<E> nextNode(Node<E> n) {
            return n.next;
        }
    }

    static final class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(E x) {
            this.item = x;
        }
    }

    public LinkedBlockingDeque() {
        this(Integer.MAX_VALUE);
    }

    public LinkedBlockingDeque(int capacity) {
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
        if (capacity > 0) {
            this.capacity = capacity;
            return;
        }
        throw new IllegalArgumentException();
    }

    private boolean linkFirst(Node<E> node) {
        if (this.count >= this.capacity) {
            return false;
        }
        Node<E> f = this.first;
        node.next = f;
        this.first = node;
        if (this.last != null) {
            f.prev = node;
        } else {
            this.last = node;
        }
        this.count++;
        this.notEmpty.signal();
        return true;
    }

    private boolean linkLast(Node<E> node) {
        if (this.count >= this.capacity) {
            return false;
        }
        Node<E> l = this.last;
        node.prev = l;
        this.last = node;
        if (this.first != null) {
            l.next = node;
        } else {
            this.first = node;
        }
        this.count++;
        this.notEmpty.signal();
        return true;
    }

    private E unlinkFirst() {
        Node<E> f = this.first;
        if (f == null) {
            return null;
        }
        Node<E> n = f.next;
        E item = f.item;
        f.item = null;
        f.next = f;
        this.first = n;
        if (n != null) {
            n.prev = null;
        } else {
            this.last = null;
        }
        this.count--;
        this.notFull.signal();
        return item;
    }

    private E unlinkLast() {
        Node<E> l = this.last;
        if (l == null) {
            return null;
        }
        Node<E> p = l.prev;
        E item = l.item;
        l.item = null;
        l.prev = l;
        this.last = p;
        if (p != null) {
            p.next = null;
        } else {
            this.first = null;
        }
        this.count--;
        this.notFull.signal();
        return item;
    }

    void unlink(Node<E> x) {
        Node<E> p = x.prev;
        Node<E> n = x.next;
        if (p == null) {
            unlinkFirst();
        } else if (n != null) {
            p.next = n;
            n.prev = p;
            x.item = null;
            this.count--;
            this.notFull.signal();
        } else {
            unlinkLast();
        }
    }

    public void addLast(E e) {
        if (!offerLast(e)) {
            throw new IllegalStateException("Deque full");
        }
    }

    public boolean offerFirst(E e) {
        if (e != null) {
            Node<E> node = new Node(e);
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                boolean linkFirst = linkFirst(node);
                return linkFirst;
            } finally {
                lock.unlock();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public boolean offerLast(E e) {
        if (e != null) {
            Node<E> node = new Node(e);
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                boolean linkLast = linkLast(node);
                return linkLast;
            } finally {
                lock.unlock();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void putLast(E e) throws InterruptedException {
        if (e != null) {
            Node<E> node = new Node(e);
            ReentrantLock lock = this.lock;
            lock.lock();
            while (!linkLast(node)) {
                try {
                    this.notFull.await();
                } finally {
                    lock.unlock();
                }
            }
            return;
        }
        throw new NullPointerException();
    }

    public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e != null) {
            Node<E> node = new Node(e);
            long nanos = unit.toNanos(timeout);
            ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            while (!linkLast(node)) {
                if (!(nanos > 0)) {
                    return false;
                }
                try {
                    nanos = this.notFull.awaitNanos(nanos);
                } finally {
                    lock.unlock();
                }
            }
            lock.unlock();
            return true;
        }
        throw new NullPointerException();
    }

    public E removeFirst() {
        E x = pollFirst();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E pollFirst() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E unlinkFirst = unlinkFirst();
            return unlinkFirst;
        } finally {
            lock.unlock();
        }
    }

    public E takeFirst() throws InterruptedException {
        E x;
        ReentrantLock lock = this.lock;
        lock.lock();
        while (true) {
            try {
                x = unlinkFirst();
                if (x != null) {
                    break;
                }
                this.notEmpty.await();
            } finally {
                lock.unlock();
            }
        }
        return x;
    }

    public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            E x = unlinkFirst();
            if (x != null) {
                lock.unlock();
                return x;
            }
            if ((nanos > 0 ? 1 : null) == null) {
                break;
            }
            try {
                nanos = this.notEmpty.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        return null;
    }

    public E getFirst() {
        E x = peekFirst();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E peekFirst() {
        E e = null;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.first != null) {
                e = this.first.item;
            }
            lock.unlock();
            return e;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public boolean removeFirstOccurrence(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = this.first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            lock.unlock();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean add(E e) {
        addLast(e);
        return true;
    }

    public boolean offer(E e) {
        return offerLast(e);
    }

    public void put(E e) throws InterruptedException {
        putLast(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return offerLast(e, timeout, unit);
    }

    public E remove() {
        return removeFirst();
    }

    public E poll() {
        return pollFirst();
    }

    public E take() throws InterruptedException {
        return takeFirst();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return pollFirst(timeout, unit);
    }

    public E element() {
        return getFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public int remainingCapacity() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.capacity - this.count;
            return i;
        } finally {
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c != this) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int n = Math.min(maxElements, this.count);
                for (int i = 0; i < n; i++) {
                    c.add(this.first.item);
                    unlinkFirst();
                }
                return n;
            } finally {
                lock.unlock();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.count;
            return i;
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = this.first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    return true;
                }
            }
            lock.unlock();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] a = new Object[this.count];
            int k = 0;
            Node<E> p = this.first;
            while (true) {
                int i = k;
                if (p == null) {
                    break;
                }
                k = i + 1;
                a[i] = p.item;
                p = p.next;
            }
            return a;
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int k;
            if (a.length < this.count) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), this.count);
            }
            int k2 = 0;
            Node<E> p = this.first;
            while (true) {
                k = k2;
                if (p == null) {
                    break;
                }
                k2 = k + 1;
                a[k] = p.item;
                p = p.next;
            }
            if (a.length > k) {
                a[k] = null;
            }
            lock.unlock();
            return a;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public String toString() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Node<E> p = this.first;
            String stringBuilder;
            if (p != null) {
                StringBuilder sb = new StringBuilder();
                sb.append('[');
                while (true) {
                    E e = p.item;
                    if (e == this) {
                        e = "(this Collection)";
                    }
                    sb.append(e);
                    p = p.next;
                    if (p == null) {
                        break;
                    }
                    sb.append(',').append(' ');
                }
                stringBuilder = sb.append(']').toString();
                return stringBuilder;
            }
            stringBuilder = "[]";
            lock.unlock();
            return stringBuilder;
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Node<E> f = this.first;
            while (f != null) {
                f.item = null;
                Node<E> n = f.next;
                f.prev = null;
                f.next = null;
                f = n;
            }
            this.last = null;
            this.first = null;
            this.count = 0;
            this.notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            s.defaultWriteObject();
            for (Node<E> p = this.first; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            s.writeObject(null);
        } finally {
            lock.unlock();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.count = 0;
        this.first = null;
        this.last = null;
        while (true) {
            E item = s.readObject();
            if (item != null) {
                add(item);
            } else {
                return;
            }
        }
    }
}
