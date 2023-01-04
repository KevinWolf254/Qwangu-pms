package org.example.linkedlist;

import java.util.ArrayList;
import java.util.List;

public class Reverse {
    public static void main(String[] args) {
        reversePrint(init());
    }

    public static void reversePrint(SinglyLinkedListNode llist) {
        // Write your code here
        if(llist != null) {
            List<Integer> data = new ArrayList<>();

            SinglyLinkedListNode temp = llist;

            while(temp != null) {
                data.add(temp.data);
                temp = temp.next;
            }

            System.out.println(data);

            for (int i = data.size() - 1; i >= 0; i--) {
                System.out.println(data.get(i));
            }
        }
    }

    static SinglyLinkedListNode init() {
        return new SinglyLinkedListNode(16,
               new SinglyLinkedListNode(12,
               new SinglyLinkedListNode(4,
               new SinglyLinkedListNode(2,
               new SinglyLinkedListNode(5)))));
    }

    static class SinglyLinkedListNode {
        int data;
        SinglyLinkedListNode next;

        public SinglyLinkedListNode(int data) {
            this.data = data;
        }

        public SinglyLinkedListNode(int data, SinglyLinkedListNode next) {
            this.data = data;
            this.next = next;
        }
    }

}
