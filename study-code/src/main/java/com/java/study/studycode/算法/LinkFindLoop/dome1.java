package com.java.study.studycode.算法.LinkFindLoop;

/**
 * @author cgm
 * @version $Id: dome1.java, v 0.1 2018-08-04 22:33 cgm Exp $$
 * 单链表找环
 */
public class dome1 {
    public static void main(String[] args) {
        Entity<Integer> e1 = new Entity<>(7, null);
        Entity<Integer> e2 = new Entity<>(6, e1);
        Entity<Integer> e3 = new Entity<>(5, e2);
        Entity<Integer> e4 = new Entity<>(4, e3);
        Entity<Integer> e5 = new Entity<>(3, e4);
        Entity<Integer> e6 = new Entity<>(2, e5);
        Entity<Integer>  e = new Entity<>(1, e6);
        e1.next = e4;
//        System.out.println(e);
        Entity<Integer> loop = findLoop(e);
        System.out.println(loop);
    }

    /**
     * 利用快慢指针寻找是否有环
     * 不改变链表结构
     * @param first
     * @param <T>
     * @return
     */
    public static  <T> Entity<T> findLoop(Entity<T> first) {
        Entity<T> slow = first;
        Entity<T> fast = first;
        boolean isHv = false;
        while (slow.next != null) {
            slow = slow.next;
            if (fast.next != null) {
                fast = fast.next.next;
            }
            if (slow == fast) {
                System.out.println("true");
                //有环
                isHv =  true;
                break;
            }
        }
        if (isHv) {
            //查找入口
            while (first != slow){
                System.out.println("find");
                first = first.next;
                slow = slow.next;
            }
            return first;
        }
        return null;
    }

    /**
     * 反转链表，如果翻转之后的表头和之前一样，则是环链，但是会破坏链表结构，
     * @param first
     * @param <T>
     */
    public static <T> void findLoop2(Entity first) {
        Entity head = first;
        Entity<T> second = head.next;
        Entity<T> next = head.next;
        head.next = null;
        while (second != null) {
            next = second.next;
            second.next = head;
            head = second;
            second = next;
            System.out.println("head ; "+head.value);
        }
        if (first.value == head.value) {
            System.out.println("loop");
        }
    }
    static class Entity<T> {
        public T value;
        public Entity<T> next;

        public Entity(T value, Entity<T> next) {
            this.value = value;
            this.next = next;
        }

        @Override
        public String toString() {
            return "Entity{" +
                    "value=" + value +
//                ", next=" + next +
                    '}';
        }
    }
}
