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
final class JsonTokenizer {
    private final List<CharBuffer> buffers;
    private final Subscriber<? super JsonToken> subscriber;
    private State state;
    private Context context;
    private final Stack stack;
    private String string = "";

    JsonTokenizer(Subscriber<? super JsonToken> subscriber) {
        this.subscriber = subscriber;
        this.buffers = new LinkedList<>();
        this.stack = new Stack();
        this.context = new ValueContext();
        transition(State.START);
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

    enum State {
        START,
        VALUE,
        FALSE_F,
        FALSE_A,
        FALSE_L,
        FALSE_S,
        FALSE_E,
        TRUE_T,
        TRUE_R,
        TRUE_U,
        TRUE_E,
        NULL_N,
        NULL_U,
        NULL_L,
        NULL_LL,
        START_OBJECT,
        START_ARRAY,
        END_OBJECT,
        END_ARRAY,
        COLON,
        COMMA,
        QUOTE,
        NUMBER,
        STRING,
        STRING_ESCAPED,
        STRING_UNICODE,
        STRING_UNICODE_1,
        STRING_UNICODE_2,
        STRING_UNICODE_3,
        KEY,
        KEY_STRING,
        KEY_STRING_ESCAPED,
        ARRAY_VALUE_OR_END,
        ARRAY_COMMA_OR_END,
        OBJECT_KEY_OR_END,
        OBJECT_COMMA_OR_END,
        END
    }

    private abstract class Context {
        Context next;
    }

    private final class ValueContext extends Context {
    }

    private final class ArrayContext extends Context {
    }

    private final class ObjectContext extends Context {
    }

    // Table to look up hex ch -> value (for e.g HEX['F'] = 15, HEX['5'] = 5)
    private final static int[] HEX = new int[128];
    static {
        Arrays.fill(HEX, -1);
        for (int i='0'; i <= '9'; i++) {
            HEX[i] = i-'0';
        }
        for (int i='A'; i <= 'F'; i++) {
            HEX[i] = 10+i-'A';
        }
        for (int i='a'; i <= 'f'; i++) {
            HEX[i] = 10+i-'a';
        }
    }
    private final static int HEX_LENGTH = HEX.length;


    void parse(CharBuffer buf) {
        buffers.add(buf);
        while(hasRemaining()) {
            _parse(buf);
        }
    }

    private void _parse(CharBuffer buf) {
        switch (state) {
            case START:
                readStart();
            case VALUE:
                readValue();
                break;
            case FALSE_F:
                readFalseF();
                break;
            case FALSE_A:
                readFalseA();
                break;
            case FALSE_L:
                readFalseL();
                break;
            case FALSE_S:
                readFalseS();
                break;
            case FALSE_E:
                readFalseE();
                break;
            case TRUE_T:
                readTrueT();
                break;
            case TRUE_R:
                readTrueR();
                break;
            case TRUE_U:
                readTrueU();
                break;
            case TRUE_E:
                readTrueE();
                break;
            case NULL_N:
                readNullN();
                break;
            case NULL_U:
                readNullU();
                break;
            case NULL_L:
                readNullL();
                break;
            case NULL_LL:
                readNullLL();
                break;
            case START_OBJECT:
                readStartObject();
                break;
            case START_ARRAY:
                readStartArray();
                break;
            case END_ARRAY:
                readEndArray();
                break;
            case END_OBJECT:
                readEndObject();
                break;
            case COLON:
                readColon();
                break;
            case COMMA:
                break;
            case QUOTE:
                break;
            case NUMBER:
                readNumber();
                break;
            case STRING:
                readString();
                break;
            case STRING_ESCAPED:
                readEscapedString();
                break;
            case STRING_UNICODE:
            case STRING_UNICODE_1:
            case STRING_UNICODE_2:
            case STRING_UNICODE_3:
                throw new RuntimeException("TODO");
            case KEY:
                readKey();
                break;
            case KEY_STRING:
                readKeyString();
                break;
            case KEY_STRING_ESCAPED:
                readEscapedKeyString();
                break;
            case ARRAY_VALUE_OR_END:
                readArrayValueOrEnd();
                break;
            case ARRAY_COMMA_OR_END:
                readArrayCommaOrEnd();
                break;
            case OBJECT_KEY_OR_END:
                readObjectKeyOrEnd();
                break;
            case OBJECT_COMMA_OR_END:
                readObjectCommaOrEnd();
                break;
            case END:
                break;
        }
    }

    private void readStart() {
        assert context instanceof ValueContext;

        transition(State.VALUE);
    }

    private void readStartObject() {
        subscriber.onNext(JsonToken.START_OBJECT);
        stack.push(context);
        context = new ObjectContext();
        transition(State.OBJECT_KEY_OR_END);
    }

    private void readObjectKeyOrEnd() {
        char ch;
        if (hasRemaining()) {
            mark();
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case '}':
                transition(State.END_OBJECT);
                break;
            default:
                reset();
                transition(State.KEY);
        }
    }

    private void readObjectCommaOrEnd() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case '}':
                transition(State.END_OBJECT);
                break;
            case ',':
                transition(State.KEY);
                break;
            default:
                throw new RuntimeException("Expecting '}' or ',' but got = " + ch);
        }
    }

    private void readEndObject() {
        subscriber.onNext(JsonToken.END_OBJECT);

        context = stack.pop();
        if (context instanceof ValueContext) {
            transition(State.VALUE);
        } else if (context instanceof ArrayContext) {
            transition(State.ARRAY_COMMA_OR_END);
        } else {
            transition(State.OBJECT_COMMA_OR_END);
        }
    }

    private void readColon() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case ':':
                transition(State.VALUE);
                break;
            default:
                throw new RuntimeException("Expecting ':'  but got = " + ch);
        }
    }

    private void readStartArray() {
        subscriber.onNext(JsonToken.START_ARRAY);

        stack.push(context);
        context = new ArrayContext();
        transition(State.ARRAY_VALUE_OR_END);
    }

    private void readArrayValueOrEnd() {
        char ch;
        if (hasRemaining()) {
            mark();
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case ']':
                transition(State.END_ARRAY);
                break;
            default:
                reset();
                transition(State.VALUE);
                break;
        }
    }

    private void readArrayCommaOrEnd() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case ']':
                transition(State.END_ARRAY);
                break;
            case ',':
                transition(State.VALUE);
                break;
            default:
                throw new RuntimeException("Expecting ']' or ',' but got = " + ch);
        }
    }

    private void readEndArray() {
        subscriber.onNext(JsonToken.END_ARRAY);

        context = stack.pop();
        if (context instanceof ValueContext) {
            transition(State.VALUE);
        } else if (context instanceof ArrayContext) {
            transition(State.ARRAY_COMMA_OR_END);
        } else {
            transition(State.OBJECT_COMMA_OR_END);
        }
    }

    private void readFalseF() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'a') {
            transition(State.FALSE_A);
        } else {
            throw new RuntimeException("Expecting f'a'lse but got = " + ch);
        }
    }

    private void readFalseA() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'l') {
            transition(State.FALSE_L);
        } else {
            throw new RuntimeException("Expecting fa'l'se but got = " + ch);
        }
    }

    private void readFalseL() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 's') {
            transition(State.FALSE_S);
        } else {
            throw new RuntimeException("Expecting fal's'e but got = " + ch);
        }
    }

    private void readFalseS() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'e') {
            transition(State.FALSE_E);
        } else {
            throw new RuntimeException("Expecting fals'e' but got = " + ch);
        }
    }

    private void readFalseE() {
        subscriber.onNext(JsonToken.VALUE_FALSE);

        if (context instanceof ValueContext) {
            transition(State.VALUE); // or space ??
        } else if (context instanceof ArrayContext) {
            transition(State.ARRAY_COMMA_OR_END);
        } else {
            transition(State.OBJECT_COMMA_OR_END);
        }
    }

    private void readTrueT() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'r') {
            transition(State.TRUE_R);
        } else {
            throw new RuntimeException("Expecting t'r'ue but got = " + ch);
        }
    }

    private void readTrueR() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'u') {
            transition(State.TRUE_U);
        } else {
            throw new RuntimeException("Expecting tr'u'e but got = " + ch);
        }
    }

    private void readTrueU() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'e') {
            transition(State.TRUE_E);
        } else {
            throw new RuntimeException("Expecting tru'e' but got = " + ch);
        }
    }

    private void readTrueE() {
        subscriber.onNext(JsonToken.VALUE_TRUE);

        if (context instanceof ValueContext) {
            transition(State.VALUE); // or space ??
        } else if (context instanceof ArrayContext) {
            transition(State.ARRAY_COMMA_OR_END);
        } else {
            transition(State.OBJECT_COMMA_OR_END);
        }
    }

    private void readNullN() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'u') {
            transition(State.NULL_U);
        } else {
            throw new RuntimeException("Expecting n'u'll but got = " + ch);
        }
    }

    private void readNullU() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'l') {
            transition(State.NULL_L);
        } else {
            throw new RuntimeException("Expecting nu'l'l but got = " + ch);
        }
    }

    private void readNullL() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == 'l') {
            transition(State.NULL_LL);
        } else {
            throw new RuntimeException("Expecting nul'l' but got = " + ch);
        }
    }

    private void readNullLL() {
        subscriber.onNext(JsonToken.VALUE_NULL);

        if (context instanceof ValueContext) {
            transition(State.VALUE); // or space ??
        } else if (context instanceof ArrayContext) {
            transition(State.ARRAY_COMMA_OR_END);
        } else {
            transition(State.OBJECT_COMMA_OR_END);
        }
    }

    private void readString() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == '"') {
            subscriber.onNext(new JsonToken(JsonToken.JsonEvent.VALUE_STRING, CharBuffer.wrap(string))); // TODO

            if (context instanceof ValueContext) {
                transition(State.VALUE); // or space ??
            } else if (context instanceof ArrayContext) {
                transition(State.ARRAY_COMMA_OR_END);
            } else {
                transition(State.OBJECT_COMMA_OR_END);
            }
        } else if (ch == '\\') {
            transition(State.STRING_ESCAPED);
        } else if (ch < 0x20) {
            throw new RuntimeException("Invalid control char = " + ch);
        } else {
            string += ch;
        }
    }

    private void readEscapedString() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }

        switch (ch) {
            case 'b':
                string += '\b';
                transition(State.STRING);
                break;
            case 't':
                string += '\t';
                transition(State.STRING);
                break;
            case 'n':
                string += '\n';
                transition(State.STRING);
                break;
            case 'f':
                string += '\f';
                transition(State.STRING);
                break;
            case 'r':
                string += '\r';
                transition(State.STRING);
                break;
            case '"':
            case '\\':
            case '/':
                string += ch;
                transition(State.STRING);
                break;
            case 'u':
                transition(State.STRING_UNICODE);
            default:
                throw new RuntimeException("Invalid char = " + ch);
        }
    }

    private void readEscapedKeyString() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }

        switch (ch) {
            case 'b':
                string += '\b';
                transition(State.KEY_STRING);
                break;
            case 't':
                string += '\t';
                transition(State.KEY_STRING);
                break;
            case 'n':
                string += '\n';
                transition(State.KEY_STRING);
                break;
            case 'f':
                string += '\f';
                transition(State.KEY_STRING);
                break;
            case 'r':
                string += '\r';
                transition(State.KEY_STRING);
                break;
            case '"':
            case '\\':
            case '/':
                string += ch;
                transition(State.KEY_STRING);
                break;
            case 'u':
                transition(State.STRING_UNICODE);
            default:
                throw new RuntimeException("Invalid char = " + ch);
        }
    }

    private void readKey() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case '"':
                string = "";
                transition(State.KEY_STRING);
                break;
            default:
                throw new RuntimeException("Expecting '\"' but got = " + ch);
        }
    }

    private void readKeyString() {
        char ch;
        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }
        if (ch == '"') {
            subscriber.onNext(new JsonToken(JsonToken.JsonEvent.KEY, CharBuffer.wrap(string)));

            if (context instanceof ValueContext) {
                throw new RuntimeException();
            } else if (context instanceof ArrayContext) {
                throw new RuntimeException();
            } else {
                transition(State.COLON);
            }
        } else if (ch == '\\') {
            transition(State.KEY_STRING_ESCAPED);
        } else if (ch < 0x20) {
            throw new RuntimeException("Invalid control char = " + ch);
        } else {
            string += ch;
        }
    }

    private void readNumber()  {
        char ch;
        if (hasRemaining()) {
            mark();
            ch = nextChar();
        } else {
            return;
        }
        switch (ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
            case 'e':
            case 'E':
            case '.':
                return;
            default:
                reset();
        }
        subscriber.onNext(new JsonToken(JsonToken.JsonEvent.VALUE_NUMBER, CharBuffer.allocate(0))); // TODO

        if (context instanceof ValueContext) {
            transition(State.VALUE); // or space ??
        } else if (context instanceof ArrayContext) {
            transition(State.ARRAY_COMMA_OR_END);
        } else {
            transition(State.OBJECT_COMMA_OR_END);
        }
    }


    private void readValue() {
        char ch;

        if (hasRemaining()) {
            ch = nextChar();
        } else {
            return;
        }

        switch (ch) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;
            case '"':
                string = "";
                transition(State.STRING);
                break;
            case '{':
                transition(State.START_OBJECT);
                break;
            case '[':
                transition(State.START_ARRAY);
                break;
            case 't':
                transition(State.TRUE_T);
                break;
            case 'f':
                transition(State.FALSE_F);
                break;
            case 'n':
                transition(State.NULL_N);
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
                transition(State.NUMBER);
                break;
            default:
                throw new RuntimeException("Expected one of value start chars, but got = " + ch);
        }
    }

    private boolean hasRemaining() {
        while(true) {
            if (buffers.isEmpty()) {
                return false;
            } else if (buffers.get(0).hasRemaining()) {
                return true;
            } else {
                buffers.remove(0);
            }
        }
    }

    private char nextChar() {
        assert hasRemaining();
        return buffers.get(0).get();
    }

    private void transition(State state) {
        //System.out.println(this.state + "->" + state);
        this.state = state;
    }

    private void mark() {
        buffers.get(0).mark();
    }

    private void reset() {
        buffers.get(0).reset();
    }
}
