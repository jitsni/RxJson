package com.github.jitsni.rx.json;

import rx.Subscriber;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 *
 * @author Jitendra Kotamraju
 */
final class JsonValuer {
    private final Subscriber<? super JsonValue> subscriber;
    private Context context;
    private final Stack stack;

    JsonValuer(Subscriber<? super JsonValue> subscriber) {
        this.subscriber = subscriber;
        this.stack = new Stack();
        this.context = new ValueContext();
    }

    private static final class Stack {
        private Context head;

        private void push(Context context) {
            context.next = head;
            head = context;
        }

        private Context pop() {
            if (head == null) {
                throw new NoSuchElementException();
            }
            Context temp = head;
            head = head.next;
            return temp;
        }

        private boolean isEmpty() {
            return head == null;
        }
    }

    private abstract class Context {
        Context next;

        abstract void parse(JsonToken token);
        abstract JsonValue value();
        abstract void add(JsonValue value);
    }

    private final class ValueContext extends Context {
        void parse(JsonToken token) {
            switch (token.event()) {
                case START_OBJECT:
                    stack.push(this);
                    context = new ObjectContext();
                    break;
                case START_ARRAY:
                    stack.push(this);
                    context = new ArrayContext();
                    break;
                case KEY:
                    throw new IllegalStateException();
                case VALUE_STRING:
                    subscriber.onNext(new JsonString(token.buffer().toString()));
                    break;
                case VALUE_NUMBER:
                    subscriber.onNext(new JsonNumber(token.buffer()));
                    break;
                case VALUE_TRUE:
                    subscriber.onNext(JsonValue.TRUE);
                    break;
                case VALUE_FALSE:
                    subscriber.onNext(JsonValue.FALSE);
                    break;
                case VALUE_NULL:
                    subscriber.onNext(JsonValue.NULL);
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    throw new IllegalStateException();
            }

        }

        JsonValue value() {
            throw new IllegalStateException();
        }

        void add(JsonValue value) {
            subscriber.onNext(value);
        }


    }

    private final class ArrayContext extends Context {
        JsonArray array = new JsonArray();

        void parse(JsonToken token) {
            switch (token.event()) {
                case START_OBJECT:
                    stack.push(this);
                    context = new ObjectContext();
                    break;
                case START_ARRAY:
                    stack.push(this);
                    context = new ArrayContext();
                    break;
                case KEY:
                    throw new IllegalStateException();
                case VALUE_STRING:
                    array.add(new JsonString(token.buffer().toString()));
                    break;
                case VALUE_NUMBER:
                    array.add(new JsonNumber(token.buffer()));
                    break;
                case VALUE_TRUE:
                    array.add(JsonValue.TRUE);
                    break;
                case VALUE_FALSE:
                    array.add(JsonValue.FALSE);
                    break;
                case VALUE_NULL:
                    array.add(JsonValue.NULL);
                    break;
                case END_ARRAY:
                    context = stack.pop();
                    context.add(array);
                    break;
                case END_OBJECT:
                    throw new IllegalStateException();

            }
        }

        JsonArray value() {
            return array;
        }

        void add(JsonValue value) {
            array.add(value);
        }

    }

    private final class ObjectContext extends Context {
        JsonObject object = new JsonObject();
        String key;

        void parse(JsonToken token) {
            switch (token.event()) {
                case START_OBJECT:
                    stack.push(this);
                    context = new ObjectContext();
                    break;
                case START_ARRAY:
                    stack.push(this);
                    context = new ArrayContext();
                    break;
                case KEY:
                    key = token.buffer().toString();
                    break;
                case VALUE_STRING:
                    object.add(key, new JsonString(token.buffer().toString()));
                    break;
                case VALUE_NUMBER:
                    object.add(key, new JsonNumber(token.buffer()));
                    break;
                case VALUE_TRUE:
                    object.add(key, JsonValue.TRUE);
                    break;
                case VALUE_FALSE:
                    object.add(key, JsonValue.FALSE);
                    break;
                case VALUE_NULL:
                    object.add(key, JsonValue.NULL);
                    break;
                case END_ARRAY:
                    throw new IllegalStateException();
                case END_OBJECT:
                    context = stack.pop();
                    context.add(object);
                    break;
            }
        }

        JsonObject value() {
            return object;
        }

        void add(JsonValue value) {
            object.add(key, value);
        }

    }

    void parse(JsonToken token) {
        System.out.println(token);
        context.parse(token);
    }
}
