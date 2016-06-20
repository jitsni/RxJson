/**
 * Copyright 2013-2014 Jitendra Kotamraju.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.jitsni.rx.json;
import rx.Observable;

import java.nio.CharBuffer;

/**
 *
 * @author Jitendra Kotamraju
 */
public class JsonToken {

    static final JsonToken START_OBJECT = new JsonToken(JsonEvent.START_OBJECT, null);
    static final JsonToken START_ARRAY = new JsonToken(JsonEvent.START_ARRAY, null);
    static final JsonToken VALUE_TRUE = new JsonToken(JsonEvent.VALUE_TRUE, null);
    static final JsonToken VALUE_FALSE = new JsonToken(JsonEvent.VALUE_FALSE, null);
    static final JsonToken VALUE_NULL = new JsonToken(JsonEvent.VALUE_NULL, null);
    static final JsonToken END_ARRAY = new JsonToken(JsonEvent.END_ARRAY, null);
    static final JsonToken END_OBJECT = new JsonToken(JsonEvent.END_OBJECT, null);

    enum JsonEvent {
        START_OBJECT,
        START_ARRAY,
        KEY,
        VALUE_STRING,
        VALUE_NUMBER,
        VALUE_TRUE,
        VALUE_FALSE,
        VALUE_NULL,
        END_ARRAY,
        END_OBJECT
    }

    private final JsonEvent event;
    private final CharBuffer buffer;

    JsonToken(JsonEvent event, CharBuffer buffer) {
        this.event = event;
        this.buffer = buffer;
    }

    public JsonEvent event() {
        return event;
    }

    public CharBuffer buffer() {
        return buffer;
    }

    @Override
    public String toString() {
        return event.toString();
    }

}
