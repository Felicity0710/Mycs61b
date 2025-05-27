package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {

    private class Node{
        public T item;
        public Node pre;
        public Node next;

        public Node(T val,Node pref,Node nxt){
            this.item=val;
            this.pre=pref;
            this.next=nxt;
        }
        public Node(Node pref,Node nxt){
            this.pre=pref;
            this.next=nxt;
        }
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        public Node now;

        public LinkedListDequeIterator(){
            now=pre_sentinel;
        }

        @Override
        public boolean hasNext(){
            return now.next!=null;
        }

        @Override
        public T next(){
            T res=now.item;
            now=now.next;
            return res;
        }
    }

    private Node pre_sentinel;
    private Node last_sentinel;
    private int size;

    public LinkedListDeque(){
        pre_sentinel=new Node(null,null);
        last_sentinel=new Node(null,null);
        pre_sentinel.next=last_sentinel;
        last_sentinel.pre=pre_sentinel;
        size=0;
    }

    @Override
    public void addFirst(T t){
        Node tmp=new Node(t, pre_sentinel, pre_sentinel.next);
        pre_sentinel.next.pre=tmp;
        pre_sentinel.next=tmp;
        size++;
    }

    @Override
    public T removeFirst(){
        if(isEmpty())return null;
        Node tmp=pre_sentinel.next;
        pre_sentinel.next=tmp.next;
        tmp.next.pre=pre_sentinel;
        size--;
        return tmp.item;
    }

    @Override
    public void addLast(T t){
        Node tmp=new Node(t, last_sentinel.pre, last_sentinel);
        last_sentinel.pre.next=tmp;
        last_sentinel.pre=tmp;
        size++;
    }

    @Override
    public T removeLast(){
        if(isEmpty())return null;
        Node tmp=last_sentinel.pre;
        last_sentinel.pre=tmp.pre;
        tmp.pre.next=last_sentinel;
        size--;
        return tmp.item;
    }

    @Override
    public T get(int idx){
        if(idx>=size || idx<0)return null;
        Node now=pre_sentinel;
        while(idx>=0){
            now=now.next;
            idx--;
        }
        return now.item;
    }

    private T getRecursiveHelper(Node now,int idx){
        if(now.next==null)return null;
        if(idx==0)return now.item;
        return getRecursiveHelper(now.next,idx-1);
    }

    public T getRecursive(int idx){
        if(idx<0)return null;
        return getRecursiveHelper(pre_sentinel.next,idx);
    }

    @Override
    public void printDeque(){
        Node now= pre_sentinel.next;
        while(now.next!=null){
            System.out.print(now.item+" ");
            now=now.next;
        }
    }

    @Override
    public int size(){
        return size;
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }
}
