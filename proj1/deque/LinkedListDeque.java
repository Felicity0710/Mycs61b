package deque;

public class LinkedListDeque<T>{
    private class Node{
        public T item;
        public Node pre=null;
        public Node next=null;

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

    private Node pre_sentinal;
    private Node last_sentinal;
    private int size;

    public LinkedListDeque(){
        pre_sentinal=new Node(null,null);
        last_sentinal=new Node(null,null);
        pre_sentinal.next=last_sentinal;
        last_sentinal.pre=pre_sentinal;
        size=0;
    }

    public void addFirst(T t){
        Node tmp=new Node(t,pre_sentinal,pre_sentinal.next);
        pre_sentinal.next.pre=tmp;
        pre_sentinal.next=tmp;
        size++;
    }

    public T removeFirst(){
        if(isEmpty())return null;
        Node tmp=pre_sentinal.next;
        pre_sentinal.next=tmp.next;
        tmp.next.pre=pre_sentinal;
        size--;
        return tmp.item;
    }

    public void addLast(T t){
        Node tmp=new Node(t,last_sentinal.pre,last_sentinal);
        last_sentinal.pre.next=tmp;
        last_sentinal.pre=tmp;
        size++;
    }

    public T removeLast(){
        if(isEmpty())return null;
        Node tmp=last_sentinal.pre;
        last_sentinal.pre=tmp.pre;
        tmp.pre.next=last_sentinal;
        size--;
        return tmp.item;
    }

    public T get(int idx){
        if(idx>=size || idx<0)return null;
        Node now=pre_sentinal;
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
        return getRecursiveHelper(pre_sentinal.next,idx);
    }

    void printDeque(){
        Node now=pre_sentinal.next;
        while(now.next!=null){
            System.out.print(now.item+" ");
            now=now.next;
        }
    }

    public boolean isEmpty(){
        return size==0;
    }

    public int size(){
        return size;
    }
}
